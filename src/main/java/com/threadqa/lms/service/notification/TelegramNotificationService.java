package com.threadqa.lms.service.notification;

import com.threadqa.lms.bot.LmsBot;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для отправки уведомлений через Telegram
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final LmsBot telegramBot;
    private final UserRepository userRepository;

    @Value("${telegram.admin.chat.id}")
    private String adminChatId;

    /**
     * Отправляет уведомление пользователю через Telegram
     */
    public void sendNotification(Long chatId, String message) {
        try {
            telegramBot.sendMessage(chatId.toString(), message);
            log.info("Отправлено уведомление в Telegram для chatId: {}", chatId);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления в Telegram для chatId: {}", chatId, e);
        }
    }

    /**
     * Отправляет уведомление администратору
     */
    public void sendNotificationToAdmin(String message) {
        try {
            telegramBot.sendMessage(adminChatId, message);
            log.info("Отправлено уведомление администратору в Telegram");
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления администратору в Telegram", e);
        }
    }

    /**
     * Отправляет уведомление всем пользователям с ролью ADMIN
     */
    public void sendNotificationToAllAdmins(String message) {
        try {
            List<User> admins = userRepository.findByRoleName("ROLE_ADMIN", null).getContent();
            
            for (User admin : admins) {
                if (admin.getTelegramChatId() != null) {
                    telegramBot.sendMessage(admin.getTelegramChatId().toString(), message);
                }
            }
            
            log.info("Отправлено уведомление всем администраторам в Telegram");
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления администраторам в Telegram", e);
        }
    }

    /**
     * Отправляет уведомление пользователю по ID
     */
    public void sendNotificationToUser(Long userId, String message) {
        try {
            userRepository.findById(userId).ifPresent(user -> {
                if (user.getTelegramChatId() != null) {
                    telegramBot.sendMessage(user.getTelegramChatId().toString(), message);
                    log.info("Отправлено уведомление пользователю {} в Telegram", userId);
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления пользователю {} в Telegram", userId, e);
        }
    }
}
