package com.threadqa.lms.service.user;

import com.threadqa.lms.dto.user.UserDTO;
import com.threadqa.lms.dto.user.UserUpdateRequest;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.UserMapper;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.auth.SessionService;
import com.threadqa.lms.service.notification.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SessionService sessionService;
    private final TelegramNotificationService telegramNotificationService;

    /**
     * Получает информацию о пользователе по ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return userMapper.toUserDTO(user);
    }

    /**
     * Обновляет информацию о пользователе
     */
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        user.setUpdatedAt(ZonedDateTime.now());
        User updatedUser = userRepository.save(user);

        return userMapper.toUserDTO(updatedUser);
    }

    /**
     * Получает список всех пользователей с пагинацией
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toUserDTO);
    }

    /**
     * Получает список заблокированных пользователей с пагинацией
     */
    public Page<UserDTO> getBlockedUsers(Pageable pageable) {
        Page<User> users = userRepository.findByIsActiveFalse(pageable);
        return users.map(userMapper::toUserDTO);
    }

    /**
     * Блокирует аккаунт пользователя
     */
    @Transactional
    public void blockUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Блокируем аккаунт
        user.setIsActive(false);
        user.setBlockReason(reason);
        user.setBlockedAt(ZonedDateTime.now());
        userRepository.save(user);

        // Деактивируем все сессии пользователя
        sessionService.deactivateAllSessions(userId);

        // Отправляем уведомление пользователю в Telegram
        if (user.getTelegramChatId() != null) {
            telegramNotificationService.sendNotification(
                    user.getTelegramChatId(),
                    "⚠️ Ваш аккаунт был заблокирован. Причина: " + reason + ". " +
                    "Пожалуйста, обратитесь в службу поддержки для разблокировки."
            );
        }

        // Отправляем уведомление администратору
        telegramNotificationService.sendNotificationToAdmin(
                "🔒 Аккаунт пользователя " + user.getEmail() + " (ID: " + userId + ") был заблокирован.\n" +
                "Причина: " + reason
        );

        log.info("Аккаунт пользователя заблокирован: {}, причина: {}", userId, reason);
    }
}
