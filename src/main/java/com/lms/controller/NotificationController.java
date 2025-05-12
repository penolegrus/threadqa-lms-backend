package com.lms.controller;

import com.lms.dto.notification.NotificationRequest;
import com.lms.dto.notification.NotificationResponse;
import com.lms.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        NotificationResponse notification = notificationService.markNotificationAsRead(notificationId, userId);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse notification = notificationService.createNotification(request);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribeToNotifications(
            @RequestParam String endpoint,
            @RequestParam String p256dh,
            @RequestParam String auth,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.subscribeToNotifications(userId, endpoint, p256dh, auth);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribeFromNotifications(
            @RequestParam String endpoint,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.unsubscribeFromNotifications(userId, endpoint);
        return ResponseEntity.ok().build();
    }
}