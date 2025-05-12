package com.lms.controller;

import com.lms.dto.auth.*;
import com.lms.dto.user.UserDTO;
import com.lms.security.UserPrincipal;
import com.lms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(authService.register(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> sendVerificationEmail(@AuthenticationPrincipal UserPrincipal principal) {
        // The email will be sent to the authenticated user
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@Valid @RequestBody EmailConfirmationRequest request) {
        authService.confirmEmail(request.getCode());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/telegram-link")
    public ResponseEntity<TelegramLinkResponse> getTelegramLink(@AuthenticationPrincipal UserPrincipal principal) {
        // Generate and return a Telegram bot link with a unique token
        String link = "https://t.me/OpenPIBot?start=" + principal.getId();
        return ResponseEntity.ok(new TelegramLinkResponse(link));
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PasswordUpdateRequest request) {
        authService.updatePassword(principal.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @Data
    @AllArgsConstructor
    private static class TelegramLinkResponse {
        private String link;
    }
}