package com.threadqa.lms.controller.assessment;


import com.threadqa.lms.dto.assessment.test.TestRequest;
import com.threadqa.lms.dto.assessment.test.TestResponse;
import com.threadqa.lms.dto.assessment.test.TestSubmissionRequest;
import com.threadqa.lms.dto.assessment.test.TestSubmissionResponse;
import com.threadqa.lms.service.assessment.TestService;
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
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/{testId}")
    public ResponseEntity<TestResponse> getTest(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "false") boolean includeQuestions) {
        TestResponse test = testService.getTest(testId, includeQuestions);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<Page<TestResponse>> getTestsByTopic(
            @PathVariable Long topicId,
            Pageable pageable) {
        Page<TestResponse> tests = testService.getTestsByTopic(topicId, pageable);
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<TestResponse>> getTestsByCourse(
            @PathVariable Long courseId,
            Pageable pageable) {
        Page<TestResponse> tests = testService.getTestsByCourse(courseId, pageable);
        return ResponseEntity.ok(tests);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TestResponse> createTest(
            @Valid @RequestBody TestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestResponse test = testService.createTest(request, userId);
        return new ResponseEntity<>(test, HttpStatus.CREATED);
    }

    @PutMapping("/{testId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TestResponse> updateTest(
            @PathVariable Long testId,
            @Valid @RequestBody TestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestResponse test = testService.updateTest(testId, request, userId);
        return ResponseEntity.ok(test);
    }

    @DeleteMapping("/{testId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteTest(
            @PathVariable Long testId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        testService.deleteTest(testId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{testId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TestSubmissionResponse> submitTest(
            @PathVariable Long testId,
            @Valid @RequestBody TestSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestSubmissionResponse submission = testService.submitTest(testId, request, userId);
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @GetMapping("/submissions/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TestSubmissionResponse> getTestSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TestSubmissionResponse submission = testService.getTestSubmission(submissionId, userId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/submissions/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TestSubmissionResponse>> getTestSubmissionsByUser(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Page<TestSubmissionResponse> submissions = testService.getTestSubmissionsByUser(userId, pageable, currentUserId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{testId}/submissions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<TestSubmissionResponse>> getTestSubmissionsByTest(
            @PathVariable Long testId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<TestSubmissionResponse> submissions = testService.getTestSubmissionsByTest(testId, pageable, userId);
        return ResponseEntity.ok(submissions);
    }
}