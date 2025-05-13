package com.threadqa.lms.controller.course;

import com.threadqa.lms.dto.course.CourseReviewRequest;
import com.threadqa.lms.dto.course.CourseReviewResponse;
import com.threadqa.lms.service.course.CourseReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CourseReviewController {

    private final CourseReviewService reviewService;

    @PostMapping
    public ResponseEntity<CourseReviewResponse> createReview(
            @Valid @RequestBody CourseReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseReviewResponse review = reviewService.createReview(request, userId);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<CourseReviewResponse>> getReviewsByCourse(
            @PathVariable Long courseId,
            Pageable pageable) {
        Page<CourseReviewResponse> reviews = reviewService.getReviewsByCourse(courseId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CourseReviewResponse>> getReviewsByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<CourseReviewResponse> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<CourseReviewResponse> getReview(@PathVariable Long reviewId) {
        CourseReviewResponse review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<CourseReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CourseReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CourseReviewResponse review = reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByCourse(@PathVariable Long courseId) {
        Double averageRating = reviewService.getAverageRatingByCourse(courseId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> getReviewCountByCourse(@PathVariable Long courseId) {
        Long count = reviewService.getReviewCountByCourse(courseId);
        return ResponseEntity.ok(count);
    }
}
