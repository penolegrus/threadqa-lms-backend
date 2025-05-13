package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.assessment.TestOptionResponse;
import com.threadqa.lms.dto.assessment.test.*;
import com.threadqa.lms.model.assessment.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestMapper {

    public TestResponse toTestResponse(
            Test test,
            Integer questionCount,
            Integer totalPoints,
            Integer submissionCount,
            List<TestQuestionResponse> questions) {

        if (test == null) {
            return null;
        }

        return TestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .topicId(test.getTopic().getId())
                .topicTitle(test.getTopic().getTitle())
                .courseId(test.getTopic().getCourse().getId())
                .courseTitle(test.getTopic().getCourse().getTitle())
                .timeLimitMinutes(test.getTimeLimitMinutes())
                .passingScore(test.getPassingScore())
                .maxAttempts(test.getMaxAttempts())
                .isRandomized(test.getIsRandomized())
                .isPublished(test.getIsPublished())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .publishedAt(test.getPublishedAt())
                .questionCount(questionCount)
                .totalPoints(totalPoints)
                .submissionCount(submissionCount)
                .questions(questions)
                .build();
    }

    public TestQuestionResponse toTestQuestionResponse(TestQuestion question) {
        if (question == null) {
            return null;
        }

        List<TestOptionResponse> options = question.getOptions().stream()
                .map(this::toTestOptionResponse)
                .collect(Collectors.toList());

        List<TestMatchingPairResponse> matchingPairs = question.getMatchingPairs().stream()
                .map(this::toTestMatchingPairResponse)
                .collect(Collectors.toList());

        return TestQuestionResponse.builder()
                .id(question.getId())
                .testId(question.getTest().getId())
                .question(question.getQuestion())
                .questionType(question.getQuestionType())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .codeSnippet(question.getCodeSnippet())
                .codeLanguage(question.getCodeLanguage())
                .options(options)
                .matchingPairs(matchingPairs)
                .build();
    }

    public TestOptionResponse toTestOptionResponse(TestOption option) {
        if (option == null) {
            return null;
        }

        return TestOptionResponse.builder()
                .id(option.getId())
                .questionId(option.getQuestion().getId())
                .content(option.getContent())
                .isCorrect(option.getIsCorrect())
                .orderIndex(option.getOrderIndex())
                .explanation(option.getExplanation())
                .build();
    }

    public TestMatchingPairResponse toTestMatchingPairResponse(TestMatchingPair pair) {
        if (pair == null) {
            return null;
        }

        return TestMatchingPairResponse.builder()
                .id(pair.getId())
                .questionId(pair.getQuestion().getId())
                .leftItem(pair.getLeftItem())
                .rightItem(pair.getRightItem())
                .orderIndex(pair.getOrderIndex())
                .build();
    }

    public TestSubmissionResponse toTestSubmissionResponse(TestSubmission submission) {
        if (submission == null) {
            return null;
        }

        List<TestAnswerResponse> answers = submission.getAnswers().stream()
                .map(this::toTestAnswerResponse)
                .collect(Collectors.toList());

        return TestSubmissionResponse.builder()
                .id(submission.getId())
                .testId(submission.getTest().getId())
                .testTitle(submission.getTest().getTitle())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getFirstName() + " " + submission.getUser().getLastName())
                .startedAt(submission.getStartedAt())
                .submittedAt(submission.getSubmittedAt())
                .score(submission.getScore())
                .isPassed(submission.getIsPassed())
                .attemptNumber(submission.getAttemptNumber())
                .answers(answers)
                .build();
    }

    public TestAnswerResponse toTestAnswerResponse(TestAnswer answer) {
        if (answer == null) {
            return null;
        }

        List<Long> selectedOptionIds = answer.getSelectedOptions().stream()
                .map(TestOption::getId)
                .collect(Collectors.toList());

        return TestAnswerResponse.builder()
                .id(answer.getId())
                .submissionId(answer.getSubmission().getId())
                .questionId(answer.getQuestion().getId())
                .questionText(answer.getQuestion().getQuestion())
                .questionType(answer.getQuestion().getQuestionType())
                .selectedOptionIds(selectedOptionIds)
                .textAnswer(answer.getTextAnswer())
                .codeAnswer(answer.getCodeAnswer())
                .pointsEarned(answer.getPointsEarned())
                .isCorrect(answer.getIsCorrect())
                .feedback(answer.getFeedback())
                .build();
    }
}
