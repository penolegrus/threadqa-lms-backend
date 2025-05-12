package com.lms.controller;

import com.lms.dto.test.TestResponse;
import com.lms.dto.test.TestSubmissionRequest;
import com.lms.dto.test.TestSubmissionResponse;
import com.lms.service.TestService;
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
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/{testId}")
    public ResponseEntity<TestResponse> getTest(@PathVariable Long testId) {
        TestResponse response = testService.getTest(testId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{testId}/start")
    public ResponseEntity<TestSubmissionResponse> startTest(
            @PathVariable Long testId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestSubmissionResponse response = testService.startTest(testId, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/submit")
    public ResponseEntity<TestSubmissionResponse> submitTest(
            @Valid @RequestBody TestSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestSubmissionResponse response = testService.submitTest(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<TestSubmissionResponse> getTestSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestSubmissionResponse response = testService.getTestSubmission(submissionId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submissions/user")
    public ResponseEntity<Page<TestSubmissionResponse>> getUserTestSubmissions(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<TestSubmissionResponse> response = testService.getUserTestSubmissions(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{testId}/submissions/best")
    public ResponseEntity<TestSubmissionResponse> getBestTestSubmission(
            @PathVariable Long testId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestSubmissionResponse response = testService.getBestTestSubmission(testId, userId);
        return ResponseEntity.ok(response);
    }
}