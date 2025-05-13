package com.threadqa.lms.controller.course;

import com.threadqa.lms.dto.course.CourseEnrollmentRequest;
import com.threadqa.lms.dto.course.CourseEnrollmentResponse;
import com.threadqa.lms.service.course.CourseEnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseEnrollmentResponse> enrollInCourse(
            @Valid @RequestBody CourseEnrollmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseEnrollmentResponse enrollment = enrollmentService.enrollInCourse(request, userId);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CourseEnrollmentResponse>> getUserEnrollments(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<CourseEnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(userId, pageable);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<CourseEnrollmentResponse>> getCourseEnrollments(
            @PathVariable Long courseId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<CourseEnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId, pageable, userId);
        return ResponseEntity.ok(enrollments);
    }

    @PatchMapping("/{enrollmentId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseEnrollmentResponse> updateProgress(
            @PathVariable Long enrollmentId,
            @RequestParam Double progress,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseEnrollmentResponse enrollment = enrollmentService.updateProgress(enrollmentId, progress, userId);
        return ResponseEntity.ok(enrollment);
    }

    @PatchMapping("/{enrollmentId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseEnrollmentResponse> markAsCompleted(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseEnrollmentResponse enrollment = enrollmentService.markAsCompleted(enrollmentId, userId);
        return ResponseEntity.ok(enrollment);
    }

    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unenrollFromCourse(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        enrollmentService.unenrollFromCourse(enrollmentId, userId);
        return ResponseEntity.noContent().build();
    }
}
