package com.lms.mapper;

import com.lms.dto.test.*;
import com.lms.model.*;
import com.lms.repository.TestOptionRepository;
import com.lms.repository.TestQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestMapper {

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    public TestResponse toTestResponse(Test test) {
        if (test == null) {
            return null;
        }

        List<TestQuestion> questions = testQuestionRepository.findByTestOrderByOrderIndexAsc(test);
        List<TestQuestionResponse> questionResponses = questions.stream()
                .map(this::toTestQuestionResponse)
                .collect(Collectors.toList());

        return TestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .topicId(test.getTopic() != null ? test.getTopic().getId() : null)
                .topicTitle(test.getTopic() != null ? test.getTopic().getTitle() : null)
                .timeLimit(test.getTimeLimit())
                .passingPercentage(test.getPassingPercentage())
                .isRandomOrder(test.getIsRandomOrder())
                .isActive(test.getIsActive())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .questions(questionResponses)
                .build();
    }

    public TestQuestionResponse toTestQuestionResponse(TestQuestion question) {
        if (question == null) {
            return null;
        }

        List<TestOption> options = testOptionRepository.findByTestQuestionOrderByOrderIndexAsc(question);
        List<TestOptionResponse> optionResponses = options.stream()
                .map(this::toTestOptionResponse)
                .collect(Collectors.toList());

        return TestQuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getType().toString())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .options(optionResponses)
                .build();
    }

    public TestOptionResponse toTestOptionResponse(TestOption option) {
        if (option == null) {
            return null;
        }

        return TestOptionResponse.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .orderIndex(option.getOrderIndex())
                .isCorrect(option.getIsCorrect())
                .feedback(option.getFeedback())
                .build();
    }

    public TestSubmissionResponse toTestSubmissionResponse(TestSubmission submission, List<TestQuestionAnswerResponse> answers) {
        if (submission == null) {
            return null;
        }

        return TestSubmissionResponse.builder()
                .id(submission.getId())
                .testId(submission.getTest().getId())
                .testTitle(submission.getTest().getTitle())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getFirstName() + " " + submission.getUser().getLastName())
                .score(submission.getScore())
                .maxScore(submission.getMaxScore())
                .percentage(submission.getPercentage())
                .isPassed(submission.getIsPassed())
                .startedAt(submission.getStartedAt())
                .submittedAt(submission.getSubmittedAt())
                .timeSpentSeconds(submission.getTimeSpentSeconds())
                .answers(answers)
                .build();
    }

    public TestQuestionAnswerResponse toTestQuestionAnswerResponse(TestQuestionAnswer answer) {
        if (answer == null) {
            return null;
        }

        TestQuestionAnswerResponse response = new TestQuestionAnswerResponse();
        response.setId(answer.getId());
        response.setQuestionId(answer.getTestQuestion().getId());
        response.setQuestionText(answer.getTestQuestion().getQuestionText());
        response.setQuestionType(answer.getTestQuestion().getType().toString());
        response.setSelectedOptionId(answer.getSelectedOptionId());
        response.setTextAnswer(answer.getTextAnswer());
        response.setCodeAnswer(answer.getCodeAnswer());
        response.setIsCorrect(answer.getIsCorrect());
        response.setScore(answer.getScore());
        response.setFeedback(answer.getFeedback());

        // Get selected option text if applicable
        if (answer.getSelectedOptionId() != null) {
            testOptionRepository.findById(answer.getSelectedOptionId())
                    .ifPresent(option -> response.setSelectedOptionText(option.getOptionText()));
        }

        // Get correct answer for multiple choice
        if (answer.getTestQuestion().getType() == QuestionType.MULTIPLE_CHOICE) {
            String correctAnswer = testOptionRepository.findByTestQuestionAndIsCorrectTrue(answer.getTestQuestion())
                    .stream()
                    .map(TestOption::getOptionText)
                    .collect(Collectors.joining(", "));
            response.setCorrectAnswer(correctAnswer);
        }

        return response;
    }
}