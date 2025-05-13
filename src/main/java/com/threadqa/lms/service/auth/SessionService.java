package com.threadqa.lms.service.auth;

import com.threadqa.lms.dto.auth.SessionInfoResponse;
import com.threadqa.lms.exception.AccountBlockedException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.exception.SuspiciousActivityException;
import com.threadqa.lms.exception.TooManySessionsException;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.model.user.UserSession;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.repository.user.UserSessionRepository;
import com.threadqa.lms.service.notification.TelegramNotificationService;
import com.threadqa.lms.util.DeviceDetectionService;
import com.threadqa.lms.util.GeoLocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GeoLocationService geoLocationService;
    private final DeviceDetectionService deviceDetectionService;
    private final TelegramNotificationService telegramNotificationService;

    private static final int MAX_ACTIVE_SESSIONS = 2;

    /**
     * Создает новую сессию пользователя
     * 
     * @param userId ID пользователя
     * @param token JWT токен
     * @param request HTTP запрос для получения информации о клиенте
     * @return созданная сессия
     */
    @Transactional
    public UserSession createSession(Long userId, String token, HttpServletRequest request) {
        // Проверяем, не заблокирован ли пользователь
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        
        if (!user.getIsActive()) {
            throw new AccountBlockedException("Аккаунт заблокирован. Обратитесь в службу поддержки.");
        }
        
        // Получаем информацию о клиенте
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Определяем местоположение и устройство
        String city = geoLocationService.getCity(ipAddress);
        String country = geoLocationService.getCountry(ipAddress);
        String deviceType = deviceDetectionService.getDeviceType(userAgent);
        String browser = deviceDetectionService.getBrowser(userAgent);
        String os = deviceDetectionService.getOperatingSystem(userAgent);
        
        // Проверяем подозрительную активность
        checkForSuspiciousActivity(userId, city, deviceType, browser);
        
        // Проверяем количество активных сессий
        long activeSessionsCount = sessionRepository.countByUserIdAndIsActiveTrue(userId);
        
        if (activeSessionsCount >= MAX_ACTIVE_SESSIONS) {
            // Если достигнут лимит, деактивируем самую старую сессию
            deactivateOldestSession(userId);
        }
        
        // Создаем новую сессию
        UserSession session = UserSession.builder()
                .userId(userId)
                .token(token)
                .ipAddress(ipAddress)
                .city(city)
                .country(country)
                .deviceType(deviceType)
                .browser(browser)
                .operatingSystem(os)
                .userAgent(userAgent)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();
        
        return sessionRepository.save(session);
    }
    
    /**
     * Проверяет наличие подозрительной активности
     */
    private void checkForSuspiciousActivity(Long userId, String city, String deviceType, String browser) {
        List<UserSession> activeSessions = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        
        if (activeSessions.isEmpty()) {
            return; // Первый вход, нет с чем сравнивать
        }
        
        // Проверяем, есть ли сессии из этого города
        boolean hasCityMatch = activeSessions.stream()
                .anyMatch(s -> city.equals(s.getCity()));
        
        // Проверяем, есть ли сессии с этого типа устройства
        boolean hasDeviceMatch = activeSessions.stream()
                .anyMatch(s -> deviceType.equals(s.getDeviceType()));
        
        // Если нет совпадений ни по городу, ни по устройству, считаем активность подозрительной
        if (!hasCityMatch && !hasDeviceMatch) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
            
            // Блокируем аккаунт
            user.setIsActive(false);
            user.setBlockedAt(LocalDateTime.now());
            user.setBlockReason("Подозрительная активность: вход из нового города с нового устройства");
            userRepository.save(user);
            
            // Деактивируем все сессии
            sessionRepository.deactivateAllUserSessions(userId);
            
            // Отправляем уведомление пользователю
            if (user.getTelegramChatId() != null) {
                telegramNotificationService.sendNotification(
                        user.getTelegramChatId(),
                        "⚠️ Ваш аккаунт заблокирован из-за подозрительной активности. " +
                        "Обнаружен вход из нового города (" + city + ") с нового устройства. " +
                        "Пожалуйста, обратитесь в службу поддержки."
                );
            }
            
            // Отправляем уведомление администратору
            telegramNotificationService.sendNotificationToAdmin(
                    "🚨 Подозрительная активность для пользователя " + user.getEmail() + " (ID: " + userId + ").\n" +
                    "Обнаружен вход из нового города (" + city + ") с нового устройства (" + deviceType + ", " + browser + ").\n" +
                    "Аккаунт автоматически заблокирован."
            );
            
            throw new SuspiciousActivityException("Обнаружена подозрительная активность. Аккаунт заблокирован.");
        }
    }
    
    /**
     * Деактивирует самую старую сессию пользователя
     */
    private void deactivateOldestSession(Long userId) {
        List<UserSession> oldestSessions = sessionRepository.findOldestActiveSessionByUserId(
                userId, PageRequest.of(0, 1));
        
        if (!oldestSessions.isEmpty()) {
            UserSession oldestSession = oldestSessions.get(0);
            oldestSession.deactivate();
            sessionRepository.save(oldestSession);
            
            log.info("Деактивирована самая старая сессия пользователя ID: {}, сессия ID: {}", 
                    userId, oldestSession.getId());
        }
    }
    
    /**
     * Обновляет время последней активности сессии
     */
    @Transactional
    public void updateSessionActivity(String token) {
        sessionRepository.findByTokenAndIsActiveTrue(token).ifPresent(session -> {
            session.updateLastActivity();
            sessionRepository.save(session);
        });
    }
    
    /**
     * Деактивирует сессию
     */
    @Transactional
    public void deactivateSession(Long sessionId, Long userId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сессия не найдена"));
        
        if (!session.getUserId().equals(userId)) {
            throw new AccessDeniedException("У вас нет прав для управления этой сессией");
        }
        
        session.deactivate();
        sessionRepository.save(session);
    }
    
    /**
     * Деактивирует все сессии пользователя, кроме текущей
     */
    @Transactional
    public void deactivateAllSessionsExceptCurrent(Long userId, Long currentSessionId) {
        sessionRepository.deactivateAllUserSessionsExcept(userId, currentSessionId);
    }
    
    /**
     * Деактивирует все сессии пользователя
     */
    @Transactional
    public void deactivateAllSessions(Long userId) {
        sessionRepository.deactivateAllUserSessions(userId);
    }
    
    /**
     * Получает информацию о сессии по токену
     */
    public Optional<UserSession> getSessionByToken(String token) {
        return sessionRepository.findByTokenAndIsActiveTrue(token);
    }
    
    /**
     * Получает список активных сессий пользователя
     */
    public Page<SessionInfoResponse> getUserActiveSessions(Long userId, Pageable pageable) {
        Page<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        
        return sessions.map(this::mapToSessionInfoResponse);
    }
    
    /**
     * Преобразует UserSession в SessionInfoResponse
     */
    private SessionInfoResponse mapToSessionInfoResponse(UserSession session) {
        return SessionInfoResponse.builder()
                .id(session.getId())
                .ipAddress(session.getIpAddress())
                .city(session.getCity())
                .country(session.getCountry())
                .deviceType(session.getDeviceType())
                .browser(session.getBrowser())
                .operatingSystem(session.getOperatingSystem())
                .createdAt(session.getCreatedAt())
                .lastActivityAt(session.getLastActivityAt())
                .build();
    }
    
    /**
     * Получает IP-адрес клиента из запроса
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
