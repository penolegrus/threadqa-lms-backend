package com.threadqa.lms.controller.admin;

import com.threadqa.lms.dto.user.UserDTO;
import com.threadqa.lms.security.CurrentUser;
import com.threadqa.lms.security.UserPrincipal;
import com.threadqa.lms.service.auth.AuthService;
import com.threadqa.lms.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления пользователями (только для администраторов)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin Users", description = "API для управления пользователями (только для администраторов)")
public class AdminUserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Получает список всех пользователей с пагинацией
     */
    @GetMapping
    @Operation(summary = "Получить список пользователей", description = "Возвращает список всех пользователей с пагинацией")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Получает информацию о конкретном пользователе
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Получить пользователя", description = "Возвращает информацию о конкретном пользователе")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Блокирует аккаунт пользователя
     */
    @PostMapping("/{userId}/block")
    @Operation(summary = "Заблокировать пользователя", description = "Блокирует аккаунт пользователя")
    public ResponseEntity<Void> blockUser(
            @PathVariable Long userId,
            @RequestParam String reason,
            @CurrentUser UserPrincipal currentUser) {
        
        // Проверка, что администратор не блокирует сам себя
        if (userId.equals(currentUser.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        userService.blockUser(userId, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Разблокирует аккаунт пользователя
     */
    @PostMapping("/{userId}/unblock")
    @Operation(summary = "Разблокировать пользователя", description = "Разблокирует аккаунт пользователя")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        authService.unblockAccount(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Получает список заблокированных пользователей
     */
    @GetMapping("/blocked")
    @Operation(summary = "Получить список заблокированных пользователей", description = "Возвращает список заблокированных пользователей с пагинацией")
    public ResponseEntity<Page<UserDTO>> getBlockedUsers(Pageable pageable) {
        Page<UserDTO> users = userService.getBlockedUsers(pageable);
        return ResponseEntity.ok(users);
    }
}
