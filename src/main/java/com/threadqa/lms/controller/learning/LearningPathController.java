package com.threadqa.lms.controller.learning;

import com.threadqa.lms.dto.learningpath.*;
import com.threadqa.lms.service.learning.LearningPathService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-paths")
public class LearningPathController {
    
    @Autowired
    private LearningPathService learningPathService;
    
    @GetMapping
    public ResponseEntity<Page<LearningPathResponse>> getAllPublishedLearningPaths(Pageable pageable) {
        return ResponseEntity.ok(learningPathService.getAllPublishedLearningPaths(pageable));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<Page<LearningPathResponse>> getFeaturedLearningPaths(Pageable pageable) {
        return ResponseEntity.ok(learningPathService.getFeaturedLearningPaths(pageable));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<LearningPathResponse>> searchLearningPaths(
            @RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(learningPathService.searchLearningPaths(keyword, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LearningPathResponse> getLearningPathById(@PathVariable Long id) {
        return ResponseEntity.ok(learningPathService.getLearningPathById(id));
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<LearningPathResponse> getLearningPathBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(learningPathService.getLearningPathBySlug(slug));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<LearningPathResponse> createLearningPath(
            @Valid @RequestBody LearningPathRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return new ResponseEntity<>(learningPathService.createLearningPath(request, userId), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<LearningPathResponse> updateLearningPath(
            @PathVariable Long id,
            @Valid @RequestBody LearningPathRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(learningPathService.updateLearningPath(id, request, userId));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLearningPath(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        learningPathService.deleteLearningPath(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{pathId}/items")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<LearningPathItemResponse> addItemToLearningPath(
            @PathVariable Long pathId,
            @Valid @RequestBody LearningPathItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return new ResponseEntity<>(learningPathService.addItemToLearningPath(pathId, request, userId), HttpStatus.CREATED);
    }
    
    @PutMapping("/{pathId}/items/{itemId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<LearningPathItemResponse> updateLearningPathItem(
            @PathVariable Long pathId,
            @PathVariable Long itemId,
            @Valid @RequestBody LearningPathItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(learningPathService.updateLearningPathItem(pathId, itemId, request, userId));
    }
    
    @DeleteMapping("/{pathId}/items/{itemId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeLearningPathItem(
            @PathVariable Long pathId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        learningPathService.removeLearningPathItem(pathId, itemId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{pathId}/enroll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserLearningPathProgressResponse> enrollInLearningPath(
            @PathVariable Long pathId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return new ResponseEntity<>(learningPathService.enrollUserInLearningPath(pathId, userId), HttpStatus.CREATED);
    }
    
    @GetMapping("/{pathId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserLearningPathProgressResponse> getLearningPathProgress(
            @PathVariable Long pathId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(learningPathService.getUserLearningPathProgress(pathId, userId));
    }
    
    @GetMapping("/enrollments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserLearningPathProgressResponse>> getUserEnrollments(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(learningPathService.getUserEnrollments(userId));
    }
    
    @PostMapping("/{pathId}/update-progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserLearningPathProgressResponse> updateUserProgress(
            @PathVariable Long pathId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(learningPathService.updateUserProgress(pathId, userId));
    }
}
