package com.threadqa.lms.controller.analytics;

import com.threadqa.lms.dto.analytics.CourseAnalyticsResponse;
import com.threadqa.lms.dto.analytics.SystemAnalyticsResponse;
import com.threadqa.lms.dto.analytics.UserAnalyticsResponse;
import com.threadqa.lms.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemAnalyticsResponse> getSystemAnalytics() {
        SystemAnalyticsResponse analytics = analyticsService.getSystemAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseAnalyticsResponse> getCourseAnalytics(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseAnalyticsResponse analytics = analyticsService.getCourseAnalytics(courseId, userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserAnalyticsResponse> getUserAnalytics(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        UserAnalyticsResponse analytics = analyticsService.getUserAnalytics(userId, currentUserId);
        return ResponseEntity.ok(analytics);
    }
}
