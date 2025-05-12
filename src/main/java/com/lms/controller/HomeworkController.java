package com.lms.controller;

import com.lms.dto.homework.*;
import com.lms.service.HomeworkService;
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
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping("/{homeworkId}")
    public ResponseEntity<HomeworkResponse> getHomework(@PathVariable Long homeworkId) {
        HomeworkResponse homework = homeworkService.getHomework(homeworkId);
        return ResponseEntity.ok(homework);
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<HomeworkResponse>> getHomeworksByTopic(@PathVariable Long topicId) {
        List<HomeworkResponse> homeworks = homeworkService.getHomeworksByTopic(topicId);
        return ResponseEntity.ok(homeworks);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkResponse> createHomework(@Valid @RequestBody HomeworkRequest request) {
        HomeworkResponse homework = homeworkService.createHomework(request);
        return new ResponseEntity<>(homework, HttpStatus.CREATED);
    }

    @PutMapping("/{homeworkId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkResponse> updateHomework(
            @PathVariable Long homeworkId,
            @Valid @RequestBody HomeworkRequest request) {
        HomeworkResponse homework = homeworkService.updateHomework(homeworkId, request);
        return ResponseEntity.ok(homework);
    }

    @DeleteMapping("/{homeworkId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteHomework(@PathVariable Long homeworkId) {
        homeworkService.deleteHomework(homeworkId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requirements")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkRequirementResponse> addHomeworkRequirement(
            @Valid @RequestBody HomeworkRequirementRequest request) {
        HomeworkRequirementResponse requirement = homeworkService.addHomeworkRequirement(request);
        return new ResponseEntity<>(requirement, HttpStatus.CREATED);
    }

    @DeleteMapping("/requirements/{requirementId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteHomeworkRequirement(@PathVariable Long requirementId) {
        homeworkService.deleteHomeworkRequirement(requirementId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submissions")
    public ResponseEntity<HomeworkSubmissionResponse> submitHomework(
            @Valid @RequestBody HomeworkSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkSubmissionResponse submission = homeworkService.submitHomework(request, userId);
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @PostMapping("/submissions/{submissionId}/files")
    public ResponseEntity<HomeworkSubmissionResponse> uploadSubmissionFiles(
            @PathVariable Long submissionId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkSubmissionResponse submission = homeworkService.uploadSubmissionFiles(submissionId, files, userId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<HomeworkSubmissionResponse> getHomeworkSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkSubmissionResponse submission = homeworkService.getHomeworkSubmission(submissionId, userId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/submissions/user")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getUserHomeworkSubmissions(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<HomeworkSubmissionResponse> submissions = homeworkService.getUserHomeworkSubmissions(userId, pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/homework/{homeworkId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getHomeworkSubmissions(
            @PathVariable Long homeworkId,
            Pageable pageable) {
        Page<HomeworkSubmissionResponse> submissions = homeworkService.getHomeworkSubmissions(homeworkId, pageable);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/submissions/{submissionId}/review")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<HomeworkSubmissionResponse> reviewHomeworkSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody HomeworkReviewRequest request) {
        HomeworkSubmissionResponse submission = homeworkService.reviewHomeworkSubmission(submissionId, request);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/submissions/{submissionId}/chat")
    public ResponseEntity<HomeworkChatMessageResponse> sendHomeworkChatMessage(
            @PathVariable Long submissionId,
            @Valid @RequestBody HomeworkChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        HomeworkChatMessageResponse message = homeworkService.sendHomeworkChatMessage(submissionId, request, userId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/submissions/{submissionId}/chat")
    public ResponseEntity<List<HomeworkChatMessageResponse>> getHomeworkChatMessages(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<HomeworkChatMessageResponse> messages = homeworkService.getHomeworkChatMessages(submissionId, userId);
        return ResponseEntity.ok(messages);
    }
}