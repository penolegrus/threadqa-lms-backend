package com.threadqa.lms.service.auth;

import com.threadqa.lms.dto.auth.AuthResponse;
import com.threadqa.lms.dto.auth.LoginRequest;
import com.threadqa.lms.dto.auth.RefreshTokenRequest;
import com.threadqa.lms.dto.auth.RegisterRequest;
import com.threadqa.lms.dto.user.UserDTO;
import com.threadqa.lms.exception.AccountBlockedException;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.UserMapper;
import com.threadqa.lms.model.user.Role;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.RoleRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final SessionService sessionService;

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Проверка, что email не занят
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email уже используется");
        }

        // Создание нового пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setCreatedAt(ZonedDateTime.now());
        user.setUpdatedAt(ZonedDateTime.now());

        // Установка ролей
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Роль не найдена: " + roleName));
                roles.add(role);
            });
        } else {
            // По умолчанию роль STUDENT
            Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new ResourceNotFoundException("Роль по умолчанию не найдена"));
            roles.add(studentRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Отправка письма для подтверждения email
        emailService.sendVerificationEmail(savedUser);

        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерация токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // Создание сессии пользователя
        sessionService.createSession(savedUser.getId(), accessToken, httpRequest);

        UserDTO userDTO = userMapper.toUserDTO(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 час в секундах
                .user(userDTO)
                .build();
    }

    /**
     * Вход пользователя в систему
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Получение пользователя
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Проверка, не заблокирован ли аккаунт
        if (!user.getIsActive()) {
            throw new AccountBlockedException("Аккаунт заблокирован. Причина: " + 
                    (user.getBlockReason() != null ? user.getBlockReason() : "Неизвестно"));
        }

        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Обновление времени последнего входа
        user.setLastLoginAt(ZonedDateTime.now());
        userRepository.save(user);

        // Генерация токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // Создание сессии пользователя
        sessionService.createSession(user.getId(), accessToken, httpRequest);

        UserDTO userDTO = userMapper.toUserDTO(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 час в секундах
                .user(userDTO)
                .build();
    }

    /**
     * Обновление токена доступа
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        // Проверка refresh токена
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadRequestException("Недействительный refresh token");
        }

        // Получение ID пользователя из токена
        String userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Проверка, не заблокирован ли аккаунт
        if (!user.getIsActive()) {
            throw new AccountBlockedException("Аккаунт заблокирован. Причина: " + 
                    (user.getBlockReason() != null ? user.getBlockReason() : "Неизвестно"));
        }

        // Создание нового authentication объекта
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName()))
                        .toList()
        );

        // Генерация новых токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // Создание новой сессии пользователя
        sessionService.createSession(user.getId(), accessToken, httpRequest);

        UserDTO userDTO = userMapper.toUserDTO(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 час в секундах
                .user(userDTO)
                .build();
    }

    /**
     * Выход пользователя из системы
     */
    @Transactional
    public void logout(String token) {
        // Деактивация сессии
        sessionService.getSessionByToken(token).ifPresent(session -> {
            session.deactivate();
            // Сохранение обновленной сессии выполняется внутри sessionService
        });

        // Очистка контекста безопасности
        SecurityContextHolder.clearContext();
    }

    /**
     * Подтверждение email пользователя
     */
    @Transactional
    public void verifyEmail(String token) {
        // Проверка токена подтверждения email
        // Здесь должна быть логика проверки токена из базы данных или другого хранилища
        
        // Для примера, просто проверяем, что токен не пустой
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Недействительный токен подтверждения");
        }
        
        // Получение пользователя по токену и подтверждение email
        // Здесь должна быть логика получения пользователя по токену
        
        // Для примера, просто обновляем статус email для пользователя с ID=1
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        
        user.setIsEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Повторная отправка письма для подтверждения email
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + email + " не найден"));
        
        if (user.getIsEmailVerified()) {
            throw new BadRequestException("Email уже подтвержден");
        }
        
        emailService.sendVerificationEmail(user);
    }

    /**
     * Разблокировка аккаунта пользователя (только для администраторов)
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void unblockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        
        user.setIsActive(true);
        user.setBlockReason(null);
        user.setBlockedAt(null);
        userRepository.save(user);
        
        log.info("Аккаунт пользователя разблокирован: {}", userId);
    }
}
