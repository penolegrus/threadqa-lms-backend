package com.threadqa.lms.controller.auth;

import com.threadqa.lms.dto.auth.SessionInfoResponse;
import com.threadqa.lms.security.CurrentUser;
import com.threadqa.lms.security.UserPrincipal;
import com.threadqa.lms.service.auth.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления сессиями пользователя
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "API для управления сессиями пользователя")
public class SessionController {

    private final SessionService sessionService;

    /**
     * Получает список активных сессий текущего пользователя
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить список активных сессий", description = "Возвращает список активных сессий текущего пользователя")
    public ResponseEntity<Page<SessionInfoResponse>> getUserSessions(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        
        Page<SessionInfoResponse> sessions = sessionService.getUserActiveSessions(currentUser.getId(), pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Завершает указанную сессию пользователя
     */
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Завершить сессию", description = "Завершает указанную сессию пользователя")
    public ResponseEntity<Void> terminateSession(
            @PathVariable Long sessionId,
            @CurrentUser UserPrincipal currentUser) {
        
        sessionService.deactivateSession(sessionId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Завершает все сессии пользователя, кроме текущей
     */
    @DeleteMapping("/all-except-current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Завершить все сессии, кроме текущей", description = "Завершает все активные сессии пользователя, кроме текущей")
    public ResponseEntity<Void> terminateAllSessionsExceptCurrent(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam Long currentSessionId) {
        
        sessionService.deactivateAllSessionsExceptCurrent(currentUser.getId(), currentSessionId);
        return ResponseEntity.ok().build();
    }
}
