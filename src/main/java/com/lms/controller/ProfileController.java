package com.lms.controller;

import com.lms.dto.course.CourseProgress;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.user.UserProfileDTO;
import com.lms.dto.user.UserUpdateRequest;
import com.lms.security.UserPrincipal;
import com.lms.service.CourseService;
import com.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(principal.getId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getId(), updateRequest));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getUserCourses(@AuthenticationPrincipal UserPrincipal principal) {
        // Get courses the user is enrolled in
        return ResponseEntity.ok(courseService.getUserCourses(principal.getId()));
    }

    @GetMapping("/progress")
    public ResponseEntity<List<CourseProgress>> getUserProgress(@AuthenticationPrincipal UserPrincipal principal) {
        // Get user's progress for all enrolled courses
        return ResponseEntity.ok(courseService.getUserProgress(principal.getId()));
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateDTO>> getUserCertificates(@AuthenticationPrincipal UserPrincipal principal) {
        // Get user's certificates
        return ResponseEntity.ok(certificateService.getUserCertificates(principal.getId()));
    }
}