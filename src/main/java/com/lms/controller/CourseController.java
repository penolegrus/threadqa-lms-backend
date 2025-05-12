package com.lms.controller;

import com.lms.dto.course.*;
import com.lms.service.CourseService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<CourseResponse> courses = courseService.getAllCourses(search, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) {
        CourseResponse course = courseService.getCourse(courseId);
        return ResponseEntity.ok(course);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseResponse course = courseService.createCourse(request, userId);
        return new ResponseEntity<>(course, HttpStatus.CREATED);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseResponse course = courseService.updateCourse(courseId, request, userId);
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/thumbnail")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> uploadCourseThumbnail(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseResponse course = courseService.uploadCourseThumbnail(courseId, file, userId);
        return ResponseEntity.ok(course);
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<CourseEnrollmentResponse> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseEnrollmentResponse enrollment = courseService.enrollInCourse(courseId, userId);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @GetMapping("/enrolled")
    public ResponseEntity<Page<CourseResponse>> getEnrolledCourses(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<CourseResponse> courses = courseService.getEnrolledCourses(userId, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/teaching")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<CourseResponse>> getTeachingCourses(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<CourseResponse> courses = courseService.getTeachingCourses(userId, pageable);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{courseId}/reviews")
    public ResponseEntity<CourseReviewResponse> addCourseReview(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseReviewResponse review = courseService.addCourseReview(courseId, request, userId);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/{courseId}/reviews")
    public ResponseEntity<Page<CourseReviewResponse>> getCourseReviews(
            @PathVariable Long courseId,
            Pageable pageable) {
        Page<CourseReviewResponse> reviews = courseService.getCourseReviews(courseId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseProgressResponse progress = courseService.getCourseProgress(courseId, userId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseResponse course = courseService.publishCourse(courseId, userId);
        return ResponseEntity.ok(course);
    }

    @PostMapping("/{courseId}/unpublish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> unpublishCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseResponse course = courseService.unpublishCourse(courseId, userId);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCourseCategories() {
        List<String> categories = courseService.getCourseCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<CourseResponse>> getFeaturedCourses() {
        List<CourseResponse> courses = courseService.getFeaturedCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CourseResponse>> getPopularCourses() {
        List<CourseResponse> courses = courseService.getPopularCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/newest")
    public ResponseEntity<List<CourseResponse>> getNewestCourses() {
        List<CourseResponse> courses = courseService.getNewestCourses();
        return ResponseEntity.ok(courses);
    }
}