package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.assessment.quiz.*;
import com.threadqa.lms.model.assessment.*;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.user.User;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public Quiz toEntity(QuizRequest request, Topic topic, User createdBy) {
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .passingScore(request.getPassingScore())
                .isActive(request.getIsActive())
                .shuffleQuestions(request.getShuffleQuestions())
                .showAnswers(request.getShowAnswers())
                .topic(topic)
                .createdBy(createdBy)
                .build();
        
        return quiz;
    }

    public QuizQuestion toEntity(QuizQuestionRequest request, Quiz quiz) {
        return QuizQuestion.builder()
                .questionText(request.getQuestionText())
                .points(request.getPoints())
                .questionType(request.getQuestionType())
                .orderIndex(request.getOrderIndex())
                .quiz(quiz)
                .build();
    }

    public QuizOption toEntity(QuizOptionRequest request, QuizQuestion question) {
        return QuizOption.builder()
                .optionText(request.getOptionText())
                .isCorrect(request.getIsCorrect())
                .orderIndex(request.getOrderIndex())
                .matchingText(request.getMatchingText())
                .question(question)
                .build();
    }

    public QuizAnswer toEntity(QuizAnswerRequest request, Quiz quiz, User user) {
        return QuizAnswer.builder()
                .quiz(quiz)
                .user(user)
                .startedAt(LocalDateTime.now())
                .score(0)
                .isPassed(false)
                .build();
    }

    public QuizQuestionAnswer toEntity(QuizQuestionAnswerRequest request, QuizAnswer quizAnswer, 
                                      QuizQuestion question, List<QuizOption> selectedOptions, Integer pointsEarned) {
        return QuizQuestionAnswer.builder()
                .quizAnswer(quizAnswer)
                .question(question)
                .textAnswer(request.getTextAnswer())
                .selectedOptions(selectedOptions)
                .pointsEarned(pointsEarned)
                .build();
    }

    public QuizResponse toResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimit(quiz.getTimeLimit())
                .passingScore(quiz.getPassingScore())
                .isActive(quiz.getIsActive())
                .shuffleQuestions(quiz.getShuffleQuestions())
                .showAnswers(quiz.getShowAnswers())
                .topicId(quiz.getTopic() != null ? quiz.getTopic().getId() : null)
                .topicTitle(quiz.getTopic() != null ? quiz.getTopic().getTitle() : null)
                .courseId(quiz.getTopic() != null && quiz.getTopic().getCourse() != null ? 
                        quiz.getTopic().getCourse().getId() : null)
                .courseTitle(quiz.getTopic() != null && quiz.getTopic().getCourse() != null ? 
                        quiz.getTopic().getCourse().getTitle() : null)
                .createdById(quiz.getCreatedBy() != null ? quiz.getCreatedBy().getId() : null)
                .createdByName(quiz.getCreatedBy() != null ? 
                        quiz.getCreatedBy().getFirstName() + " " + quiz.getCreatedBy().getLastName() : null)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .questions(quiz.getQuestions().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public QuizQuestionResponse toResponse(QuizQuestion question) {
        return QuizQuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .points(question.getPoints())
                .questionType(question.getQuestionType())
                .orderIndex(question.getOrderIndex())
                .options(question.getOptions().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public QuizOptionResponse toResponse(QuizOption option) {
        return QuizOptionResponse.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .isCorrect(option.getIsCorrect())
                .orderIndex(option.getOrderIndex())
                .matchingText(option.getMatchingText())
                .build();
    }

    public QuizAnswerResponse toResponse(QuizAnswer answer, List<QuizQuestionAnswer> questionAnswers) {
        Integer totalPoints = answer.getQuiz().getQuestions().stream()
                .mapToInt(QuizQuestion::getPoints)
                .sum();
                
        Integer earnedPoints = questionAnswers.stream()
                .mapToInt(QuizQuestionAnswer::getPointsEarned)
                .sum();
                
        Integer timeSpentMinutes = null;
        if (answer.getCompletedAt() != null) {
            timeSpentMinutes = (int) Duration.between(answer.getStartedAt(), answer.getCompletedAt()).toMinutes();
        }
        
        return QuizAnswerResponse.builder()
                .id(answer.getId())
                .quizId(answer.getQuiz().getId())
                .quizTitle(answer.getQuiz().getTitle())
                .userId(answer.getUser().getId())
                .userName(answer.getUser().getFirstName() + " " + answer.getUser().getLastName())
                .startedAt(answer.getStartedAt())
                .completedAt(answer.getCompletedAt())
                .score(answer.getScore())
                .isPassed(answer.getIsPassed())
                .feedback(answer.getFeedback())
                .questionAnswers(questionAnswers.stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .timeSpentMinutes(timeSpentMinutes)
                .totalPoints(totalPoints)
                .earnedPoints(earnedPoints)
                .build();
    }

    public QuizQuestionAnswerResponse toResponse(QuizQuestionAnswer questionAnswer) {
        QuizQuestion question = questionAnswer.getQuestion();
        List<QuizOption> correctOptions = question.getOptions().stream()
                .filter(QuizOption::getIsCorrect)
                .collect(Collectors.toList());
                
        boolean isCorrect = questionAnswer.getPointsEarned().equals(question.getPoints());
        
        return QuizQuestionAnswerResponse.builder()
                .id(questionAnswer.getId())
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .textAnswer(questionAnswer.getTextAnswer())
                .pointsEarned(questionAnswer.getPointsEarned())
                .maxPoints(question.getPoints())
                .selectedOptions(questionAnswer.getSelectedOptions().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .correctOptions(correctOptions.stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .isCorrect(isCorrect)
                .build();
    }
}
