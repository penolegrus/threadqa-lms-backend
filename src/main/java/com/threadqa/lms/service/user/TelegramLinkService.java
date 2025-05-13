package com.threadqa.lms.service.user;

import com.threadqa.lms.dto.user.TelegramLinkResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramLinkService {

    private final UserRepository userRepository;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    
    @Transactional
    public TelegramLinkResponse generateTelegramLink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Генерируем уникальный код для пользователя
        String confirmationCode = generateUniqueCode();
        user.setTelegramConfirmationCode(confirmationCode);
        userRepository.save(user);
        
        // Формируем ссылку на Telegram бота
        String telegramLink = String.format("https://t.me/%s?start=%s", botUsername, confirmationCode);
        
        return TelegramLinkResponse.builder()
                .telegramLink(telegramLink)
                .confirmationCode(confirmationCode)
                .isTelegramLinked(user.getTelegramChatId() != null)
                .build();
    }
    
    private String generateUniqueCode() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    
    @Transactional
    public void unlinkTelegram(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setTelegramChatId(null);
        user.setTelegramUserName(null);
        user.setTelegramConfirmationCode(null);
        userRepository.save(user);
        
        log.info("Telegram account unlinked for user: {}", user.getEmail());
    }
}
