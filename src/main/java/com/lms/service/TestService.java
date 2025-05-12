package com.lms.service;

import com.lms.dto.test.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.TestMapper;
import com.lms.model.*;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final TestSubmissionRepository testSubmissionRepository;
    private final TestQuestionAnswerRepository testQuestionAnswerRepository;
    private final UserRepository userRepository;
    private final TestMapper testMapper;

    @Transactional(readOnly = true)
    public TestResponse getTest(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        return testMapper.toTestResponse(test);
    }

    @Transactional
    public TestSubmissionResponse startTest(Long testId, Long userId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create a new test submission
        TestSubmission submission = TestSubmission.builder()
                .test(test)
                .user(user)
                .maxScore(calculateMaxScore(test))
                .startedAt(ZonedDateTime.now())
                .build();

        TestSubmission savedSubmission = testSubmissionRepository.save(submission);

        return testMapper.toTestSubmissionResponse(savedSubmission, new ArrayList<>());
    }

    @Transactional
    public TestSubmissionResponse submitTest(TestSubmissionRequest request, Long userId) {
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find the active submission or create a new one
        List<TestSubmission> submissions = testSubmissionRepository.findByUserAndTest(user, test);
        TestSubmission submission = submissions.stream()
                .filter(s -> s.getSubmittedAt() == null)
                .findFirst()
                .orElseGet(() -> {
                    TestSubmission newSubmission = TestSubmission.builder()
                            .test(test)
                            .user(user)
                            .maxScore(calculateMaxScore(test))
                            .startedAt(ZonedDateTime.now())
                            .build();
                    return testSubmissionRepository.save(newSubmission);
                });

        // Process answers
        List<TestQuestionAnswer> answers = new ArrayList<>();
        int totalScore = 0;

        for (TestQuestionAnswerRequest answerRequest : request.getAnswers()) {
            TestQuestion question = testQuestionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

            TestQuestionAnswer answer = processAnswer(question, answerRequest, submission);
            answers.add(answer);

            if (answer.getIsCorrect() != null && answer.getIsCorrect()) {
                totalScore += answer.getScore();
            }
        }

        // Update submission
        submission.setScore(totalScore);
        submission.setPercentage((double) totalScore / submission.getMaxScore() * 100);
        submission.setIsPassed(submission.getPercentage() >= test.getPassingPercentage());
        submission.setSubmittedAt(ZonedDateTime.now());
        submission.setTimeSpentSeconds(request.getTimeSpentSeconds());

        TestSubmission savedSubmission = testSubmissionRepository.save(submission);

        List<TestQuestionAnswerResponse> answerResponses = answers.stream()
                .map(testMapper::toTestQuestionAnswerResponse)
                .collect(Collectors.toList());

        return testMapper.toTestSubmissionResponse(savedSubmission, answerResponses);
    }

    private TestQuestionAnswer processAnswer(TestQuestion question, TestQuestionAnswerRequest answerRequest, TestSubmission submission) {
        TestQuestionAnswer answer = TestQuestionAnswer.builder()
                .testSubmission(submission)
                .testQuestion(question)
                .selectedOptionId(answerRequest.getSelectedOptionId())
                .textAnswer(answerRequest.getTextAnswer())
                .codeAnswer(answerRequest.getCodeAnswer())
                .build();

        // Evaluate answer based on question type
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                evaluateMultipleChoiceAnswer(answer);
                break;
            case TEXT:
                evaluateTextAnswer(answer);
                break;
            case CODE:
                evaluateCodeAnswer(answer);
                break;
            case MATCHING:
                evaluateMatchingAnswer(answer);
                break;
        }

        return testQuestionAnswerRepository.save(answer);
    }

    private void evaluateMultipleChoiceAnswer(TestQuestionAnswer answer) {
        TestQuestion question = answer.getTestQuestion();
        Long selectedOptionId = answer.getSelectedOptionId();

        if (selectedOptionId == null) {
            answer.setIsCorrect(false);
            answer.setScore(0);
            answer.setFeedback("No option selected");
            return;
        }

        TestOption selectedOption = testOptionRepository.findById(selectedOptionId)
                .orElse(null);

        if (selectedOption == null || !selectedOption.getTestQuestion().getId().equals(question.getId())) {
            answer.setIsCorrect(false);
            answer.setScore(0);
            answer.setFeedback("Invalid option selected");
            return;
        }

        answer.setIsCorrect(selectedOption.getIsCorrect());
        answer.setScore(selectedOption.getIsCorrect() ? question.getPoints() : 0);
        answer.setFeedback(selectedOption.getFeedback());
    }

    private void evaluateTextAnswer(TestQuestionAnswer answer) {
        TestQuestion question = answer.getTestQuestion();
        String textAnswer = answer.getTextAnswer();

        if (textAnswer == null || textAnswer.trim().isEmpty()) {
            answer.setIsCorrect(false);
            answer.setScore(0);
            answer.setFeedback("No answer provided");
            return;
        }

        // For text answers, we'll need manual grading in a real system
        // For now, we'll mark it as pending review
        answer.setIsCorrect(null);
        answer.setScore(null);
        answer.setFeedback("Pending review");
    }

    private void evaluateCodeAnswer(TestQuestionAnswer answer) {
        TestQuestion question = answer.getTestQuestion();
        String codeAnswer = answer.getCodeAnswer();

        if (codeAnswer == null || codeAnswer.trim().isEmpty()) {
            answer.setIsCorrect(false);
            answer.setScore(0);
            answer.setFeedback("No code provided");
            return;
        }

        // For code answers, we would typically run test cases
        // For now, we'll mark it as pending review
        answer.setIsCorrect(null);
        answer.setScore(null);
        answer.setFeedback("Pending review");
    }

    private void evaluateMatchingAnswer(TestQuestionAnswer answer) {
        // Matching questions would require a more complex data structure
        // For now, we'll mark it as pending review
        answer.setIsCorrect(null);
        answer.setScore(null);
        answer.setFeedback("Pending review");
    }

    @Transactional(readOnly = true)
    public TestSubmissionResponse getTestSubmission(Long submissionId, Long userId) {
        TestSubmission submission = testSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Test submission not found"));

        // Check if user is authorized to view this submission
        if (!submission.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to view this submission");
        }

        List<TestQuestionAnswer> answers = testQuestionAnswerRepository.findByTestSubmission(submission);
        List<TestQuestionAnswerResponse> answerResponses = answers.stream()
                .map(testMapper::toTestQuestionAnswerResponse)
                .collect(Collectors.toList());

        return testMapper.toTestSubmissionResponse(submission, answerResponses);
    }

    @Transactional(readOnly = true)
    public Page<TestSubmissionResponse> getUserTestSubmissions(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<TestSubmission> submissions = testSubmissionRepository.findByUser(user, pageable);

        return submissions.map(submission -> {
            List<TestQuestionAnswer> answers = testQuestionAnswerRepository.findByTestSubmission(submission);
            List<TestQuestionAnswerResponse> answerResponses = answers.stream()
                    .map(testMapper::toTestQuestionAnswerResponse)
                    .collect(Collectors.toList());
            return testMapper.toTestSubmissionResponse(submission, answerResponses);
        });
    }

    @Transactional(readOnly = true)
    public TestSubmissionResponse getBestTestSubmission(Long testId, Long userId) {
        testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<TestSubmission> submissions = testSubmissionRepository.findBestSubmissionByUserAndTest(
                userId, testId, PageRequest.of(0, 1));

        if (submissions.isEmpty()) {
            throw new ResourceNotFoundException("No submissions found for this test");
        }

        TestSubmission bestSubmission = submissions.get(0);
        List<TestQuestionAnswer> answers = testQuestionAnswerRepository.findByTestSubmission(bestSubmission);
        List<TestQuestionAnswerResponse> answerResponses = answers.stream()
                .map(testMapper::toTestQuestionAnswerResponse)
                .collect(Collectors.toList());

        return testMapper.toTestSubmissionResponse(bestSubmission, answerResponses);
    }

    private int calculateMaxScore(Test test) {
        return testQuestionRepository.findByTest(test).stream()
                .mapToInt(TestQuestion::getPoints)
                .sum();
    }
}