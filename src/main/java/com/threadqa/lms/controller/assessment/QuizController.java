package com.threadqa.lms.controller.assessment;

import com.threadqa.lms.dto.assessment.quiz.*;
import com.threadqa.lms.service.assessment.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Controller", description = "API for managing quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Create a new quiz")
    public ResponseEntity<QuizResponse> createQuiz(
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizResponse response = quizService.createQuiz(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update an existing quiz")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizResponse response = quizService.updateQuiz(quizId, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get quiz details for instructors")
    public ResponseEntity<QuizResponse> getQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizResponse response = quizService.getQuiz(quizId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{quizId}/student")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get quiz details for students")
    public ResponseEntity<QuizResponse> getQuizForStudent(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizResponse response = quizService.getQuizForStudent(quizId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topic/{topicId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all quizzes for a topic")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByTopic(
            @PathVariable Long topicId,
            Pageable pageable) {
        Page<QuizResponse> responses = quizService.getQuizzesByTopic(topicId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/topic/{topicId}/active")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get active quizzes for a topic")
    public ResponseEntity<List<QuizResponse>> getActiveQuizzesByTopic(
            @PathVariable Long topicId) {
        List<QuizResponse> responses = quizService.getActiveQuizzesByTopic(topicId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}/active")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get active quizzes for a course")
    public ResponseEntity<List<QuizResponse>> getActiveQuizzesByCourse(
            @PathVariable Long courseId) {
        List<QuizResponse> responses = quizService.getActiveQuizzesByCourse(courseId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/creator")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get quizzes created by the current user")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByCreator(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<QuizResponse> responses = quizService.getQuizzesByCreator(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Search quizzes")
    public ResponseEntity<Page<QuizResponse>> searchQuizzes(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<QuizResponse> responses = quizService.searchQuizzes(searchTerm, pageable);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a quiz")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        quizService.deleteQuiz(quizId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/start")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Start a quiz attempt")
    public ResponseEntity<QuizAnswerResponse> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizAnswerResponse response = quizService.startQuiz(quizId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{quizId}/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Submit a quiz attempt")
    public ResponseEntity<QuizAnswerResponse> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizAnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizAnswerResponse response = quizService.submitQuiz(quizId, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts/user")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get quiz attempts for the current user")
    public ResponseEntity<Page<QuizAnswerResponse>> getUserQuizAttempts(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<QuizAnswerResponse> responses = quizService.getUserQuizAttempts(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{quizId}/attempts")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get all attempts for a quiz")
    public ResponseEntity<Page<QuizAnswerResponse>> getQuizAttempts(
            @PathVariable Long quizId,
            Pageable pageable) {
        Page<QuizAnswerResponse> responses = quizService.getQuizAttempts(quizId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get details of a quiz attempt")
    public ResponseEntity<QuizAnswerResponse> getQuizAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizAnswerResponse response = quizService.getQuizAttempt(attemptId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attempts/{attemptId}/feedback")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Provide feedback for a quiz attempt")
    public ResponseEntity<QuizAnswerResponse> provideFeedback(
            @PathVariable Long attemptId,
            @RequestParam String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizAnswerResponse response = quizService.provideFeedback(attemptId, feedback, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{quizId}/statistics")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get statistics for a quiz")
    public ResponseEntity<QuizStatisticsResponse> getQuizStatistics(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QuizStatisticsResponse response = quizService.getQuizStatistics(quizId, userId);
        return ResponseEntity.ok(response);
    }
}
