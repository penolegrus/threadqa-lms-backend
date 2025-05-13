package com.threadqa.lms.controller.progress;

import com.threadqa.lms.dto.progress.*;
import com.threadqa.lms.model.progress.UserActivity;
import com.threadqa.lms.service.progress.ProgressTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressTrackingController {

    private final ProgressTrackingService progressTrackingService;

    @PostMapping("/track")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProgressResponse> trackProgress(
            @Valid @RequestBody UserProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProgressResponse response = progressTrackingService.trackProgress(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activity/{activityType}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserActivityResponse> trackActivity(
            @PathVariable UserActivity.ActivityType activityType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String description,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        UserActivityResponse response = progressTrackingService.trackActivity(
                activityType, entityType, entityId, description, ipAddress, userAgent, userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/engagement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserEngagementResponse> trackEngagement(
            @RequestParam(required = false) Long courseId,
            @RequestParam Long sessionDurationSeconds,
            @RequestParam(required = false, defaultValue = "0") Integer pageViews,
            @RequestParam(required = false, defaultValue = "0") Integer interactions,
            @RequestParam(required = false, defaultValue = "0") Integer comments,
            @RequestParam(required = false, defaultValue = "0") Integer questionsAsked,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        
        UserEngagementResponse response = progressTrackingService.trackEngagement(
                courseId, sessionDurationSeconds, pageViews, interactions, comments, questionsAsked, userId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserProgressResponse>> getUserProgress(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Page<UserProgressResponse> progress = progressTrackingService.getUserProgressByUser(userId, pageable, currentUserId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserProgressResponse>> getUserProgressByCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Page<UserProgressResponse> progress = progressTrackingService.getUserProgressByCourse(userId, courseId, pageable, currentUserId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/user/{userId}/activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserActivityResponse>> getUserActivities(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Page<UserActivityResponse> activities = progressTrackingService.getUserActivities(userId, pageable, currentUserId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/user/{userId}/engagements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserEngagementResponse>> getUserEngagements(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Page<UserEngagementResponse> engagements = progressTrackingService.getUserEngagements(userId, pageable, currentUserId);
        return ResponseEntity.ok(engagements);
    }

    @GetMapping("/user/{userId}/course/{courseId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProgressSummaryResponse> getUserProgressSummary(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        UserProgressSummaryResponse summary = progressTrackingService.getUserProgressSummary(userId, courseId, currentUserId);
        return ResponseEntity.ok(summary);
    }
}
