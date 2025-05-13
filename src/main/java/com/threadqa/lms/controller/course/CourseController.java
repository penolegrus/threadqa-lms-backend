package com.threadqa.lms.controller.course;

import com.threadqa.lms.dto.course.CourseRequest;
import com.threadqa.lms.dto.course.CourseResponse;
import com.threadqa.lms.service.course.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<CourseResponse> courses = courseService.getAllCourses(pageable, userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/published")
    public ResponseEntity<Page<CourseResponse>> getPublishedCourses(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<CourseResponse> courses = courseService.getPublishedCourses(pageable, userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<CourseResponse>> getFeaturedCourses(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<CourseResponse> courses = courseService.getFeaturedCourses(pageable, userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CourseResponse>> searchCourses(
            @RequestParam String keyword,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<CourseResponse> courses = courseService.searchCourses(keyword, pageable, userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<CourseResponse>> getCoursesByCategory(
            @PathVariable Long categoryId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<CourseResponse> courses = courseService.getCoursesByCategory(categoryId, pageable, userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<Page<CourseResponse>> getCoursesByInstructor(
            @PathVariable Long instructorId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<CourseResponse> courses = courseService.getCoursesByInstructor(instructorId, pageable, userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        CourseResponse course = courseService.getCourse(courseId, userId);
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
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        courseService.deleteCourse(courseId, userId);
        return ResponseEntity.noContent().build();
    }
}
