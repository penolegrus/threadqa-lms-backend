package com.threadqa.lms.controller.auth;

import com.threadqa.lms.dto.auth.AuthResponse;
import com.threadqa.lms.dto.auth.LoginRequest;
import com.threadqa.lms.dto.auth.RefreshTokenRequest;
import com.threadqa.lms.dto.auth.RegisterRequest;
import com.threadqa.lms.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.register(request, httpRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email успешно подтвержден");
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(
            @RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Письмо с подтверждением отправлено повторно");
    }
}
