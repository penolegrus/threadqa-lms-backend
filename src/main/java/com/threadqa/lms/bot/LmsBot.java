package com.threadqa.lms.bot;

import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LmsBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            
            if (messageText.startsWith("/start")) {
                String[] parts = messageText.split(" ");
                if (parts.length > 1) {
                    String confirmationCode = parts[1];
                    processConfirmationCode(confirmationCode, chatId);
                } else {
                    sendWelcomeMessage(chatId);
                }
            }
        }
    }

    private void processConfirmationCode(String confirmationCode, String chatId) {
        try {
            userRepository.findByTelegramConfirmationCode(confirmationCode)
                .ifPresent(user -> {
                    user.setTelegramChatId(Long.parseLong(chatId));
                    user.setTelegramConfirmationCode(null);
                    userRepository.save(user);

                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Ваш Telegram аккаунт успешно привязан к LMS!");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        log.error("Ошибка при отправке сообщения пользователю", e);
                    }
                });
        } catch (Exception e) {
            log.error("Ошибка при обработке кода подтверждения", e);
        }
    }

    private void sendWelcomeMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Добро пожаловать в бот ThreadQA LMS!\n\n" +
                "Для привязки аккаунта используйте ссылку из личного кабинета.");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке приветственного сообщения", e);
        }
    }

    public void sendMessage(String chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            message.enableHtml(true);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю", e);
        }
    }
}
