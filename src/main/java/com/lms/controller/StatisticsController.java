package com.lms.controller;

import com.lms.dto.statistics.*;
import com.lms.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardStats> getAdminDashboardStats() {
        AdminDashboardStats stats = statisticsService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/instructor/dashboard")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<InstructorDashboardStats> getInstructorDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        InstructorDashboardStats stats = statisticsService.getInstructorDashboardStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/student/dashboard")
    public ResponseEntity<StudentDashboardStats> getStudentDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StudentDashboardStats stats = statisticsService.getStudentDashboardStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseStatistics> getCourseStatistics(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseStatistics stats = statisticsService.getCourseStatistics(courseId, userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueStatistics> getRevenueStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        RevenueStatistics stats = statisticsService.getRevenueStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserActivityStatistics> getUserActivityStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        UserActivityStatistics stats = statisticsService.getUserActivityStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/course-completion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseCompletionStatistics> getCourseCompletionStatistics() {
        CourseCompletionStatistics stats = statisticsService.getCourseCompletionStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/test-performance")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TestPerformanceStatistics> getTestPerformanceStatistics(
            @RequestParam Long courseId) {
        TestPerformanceStatistics stats = statisticsService.getTestPerformanceStatistics(courseId);
        return ResponseEntity.ok(stats);
    }
}