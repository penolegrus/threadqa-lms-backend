package com.threadqa.lms.controller.homework;

import com.threadqa.lms.dto.homework.*;
import com.threadqa.lms.service.homework.HomeworkService;
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
@RequestMapping("/api/homeworks")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<Page<HomeworkResponse>> getHomeworksByTopic(
            @PathVariable Long topicId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<HomeworkResponse> homeworks = homeworkService.getHomeworksByTopic(topicId, pageable, userId);
        return ResponseEntity.ok(homeworks);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<HomeworkResponse>> getHomeworksByCourse(
            @PathVariable Long courseId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        Page<HomeworkResponse> homeworks = homeworkService.getHomeworksByCourse(courseId, pageable, userId);
        return ResponseEntity.ok(homeworks);
    }

    @GetMapping("/{homeworkId}")
    public ResponseEntity<HomeworkResponse> getHomework(
            @PathVariable Long homeworkId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        HomeworkResponse homework = homeworkService.getHomework(homeworkId, userId);
        return ResponseEntity.ok(homework);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkResponse> createHomework(
            @Valid @RequestBody HomeworkRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkResponse homework = homeworkService.createHomework(request, userId);
        return new ResponseEntity<>(homework, HttpStatus.CREATED);
    }

    @PutMapping("/{homeworkId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkResponse> updateHomework(
            @PathVariable Long homeworkId,
            @Valid @RequestBody HomeworkRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkResponse homework = homeworkService.updateHomework(homeworkId, request, userId);
        return ResponseEntity.ok(homework);
    }

    @DeleteMapping("/{homeworkId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteHomework(
            @PathVariable Long homeworkId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        homeworkService.deleteHomework(homeworkId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HomeworkSubmissionResponse> submitHomework(
            @Valid @RequestBody HomeworkSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkSubmissionResponse submission = homeworkService.submitHomework(request, userId);
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @GetMapping("/{homeworkId}/submissions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getSubmissionsByHomework(
            @PathVariable Long homeworkId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<HomeworkSubmissionResponse> submissions = homeworkService.getSubmissionsByHomework(homeworkId, pageable, userId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getSubmissionsByUser(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Page<HomeworkSubmissionResponse> submissions = homeworkService.getSubmissionsByUser(userId, pageable, currentUserId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HomeworkSubmissionResponse> getSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkSubmissionResponse submission = homeworkService.getSubmission(submissionId, userId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/submissions/{submissionId}/review")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkSubmissionResponse> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody HomeworkReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkSubmissionResponse submission = homeworkService.reviewSubmission(submissionId, request, userId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/chat/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HomeworkChatMessageResponse> sendChatMessage(
            @Valid @RequestBody HomeworkChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkChatMessageResponse message = homeworkService.sendChatMessage(request, userId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/submissions/{submissionId}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HomeworkChatMessageResponse>> getChatMessages(
            @PathVariable Long submissionId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<HomeworkChatMessageResponse> messages = homeworkService.getChatMessages(submissionId, pageable, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/submissions/{submissionId}/chat/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markChatMessagesAsRead(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        homeworkService.markChatMessagesAsRead(submissionId, userId);
        return ResponseEntity.noContent().build();
    }
}
