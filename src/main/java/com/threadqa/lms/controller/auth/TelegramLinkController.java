package com.threadqa.lms.controller.auth;

import com.threadqa.lms.dto.user.TelegramLinkResponse;
import com.threadqa.lms.service.user.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TelegramLinkController {

    private final TelegramLinkService telegramLinkService;

    @GetMapping("/telegram-link")
    public ResponseEntity<TelegramLinkResponse> getTelegramLink(@AuthenticationPrincipal UserDetails userDetails) {
        // Получаем ID пользователя из UserDetails
        Long userId = Long.parseLong(userDetails.getUsername());
        TelegramLinkResponse response = telegramLinkService.generateTelegramLink(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/telegram-link")
    public ResponseEntity<Void> unlinkTelegram(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        telegramLinkService.unlinkTelegram(userId);
        return ResponseEntity.noContent().build();
    }
}
