package com.lms.controller;

import com.lms.dto.analytics.*;
import com.lms.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/user/{userId}/learning-path")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<UserLearningPathAnalytics> getUserLearningPathAnalytics(@PathVariable Long userId) {
        UserLearningPathAnalytics analytics = analyticsService.getUserLearningPathAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/learning-path")
    public ResponseEntity<UserLearningPathAnalytics> getCurrentUserLearningPathAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserLearningPathAnalytics analytics = analyticsService.getUserLearningPathAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/course/{courseId}/engagement")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseEngagementAnalytics> getCourseEngagementAnalytics(
            @PathVariable Long courseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        CourseEngagementAnalytics analytics = analyticsService.getCourseEngagementAnalytics(courseId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/course/{courseId}/content")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseContentAnalytics> getCourseContentAnalytics(@PathVariable Long courseId) {
        CourseContentAnalytics analytics = analyticsService.getCourseContentAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/{userId}/progress")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<UserProgressAnalytics> getUserProgressAnalytics(@PathVariable Long userId) {
        UserProgressAnalytics analytics = analyticsService.getUserProgressAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/progress")
    public ResponseEntity<UserProgressAnalytics> getCurrentUserProgressAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProgressAnalytics analytics = analyticsService.getUserProgressAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/course/{courseId}/completion")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<UserCourseCompletionAnalytics>> getCourseCompletionAnalytics(@PathVariable Long courseId) {
        List<UserCourseCompletionAnalytics> analytics = analyticsService.getCourseCompletionAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/instructor/{instructorId}/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorPerformanceAnalytics> getInstructorPerformanceAnalytics(@PathVariable Long instructorId) {
        InstructorPerformanceAnalytics analytics = analyticsService.getInstructorPerformanceAnalytics(instructorId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/instructor/performance")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<InstructorPerformanceAnalytics> getCurrentInstructorPerformanceAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        InstructorPerformanceAnalytics analytics = analyticsService.getInstructorPerformanceAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/platform/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlatformOverviewAnalytics> getPlatformOverviewAnalytics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        PlatformOverviewAnalytics analytics = analyticsService.getPlatformOverviewAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
}