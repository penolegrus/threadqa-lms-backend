package com.threadqa.lms.service.notification;

import com.threadqa.lms.bot.LmsBot;
import com.threadqa.lms.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final LmsBot lmsBot;

    public void sendNotification(User user, String message) {
        if (user.getTelegramChatId() != null) {
            lmsBot.sendMessage(user.getTelegramChatId().toString(), message);
            log.info("Telegram notification sent to user: {}", user.getEmail());
        } else {
            log.info("User {} doesn't have Telegram chat ID", user.getEmail());
        }
    }

    public void sendNotificationToAll(Iterable<User> users, String message) {
        users.forEach(user -> {
            if (user.getTelegramChatId() != null) {
                lmsBot.sendMessage(user.getTelegramChatId().toString(), message);
            }
        });
        log.info("Telegram notification sent to multiple users");
    }
}
