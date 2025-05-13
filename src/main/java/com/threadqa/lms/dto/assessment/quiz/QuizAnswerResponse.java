package com.threadqa.lms.dto.assessment.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResponse {

    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long userId;
    private String userName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer score;
    private Boolean isPassed;
    private String feedback;
    private List<QuizQuestionAnswerResponse> questionAnswers = new ArrayList<>();
    private Integer timeSpentMinutes;
    private Integer totalPoints;
    private Integer earnedPoints;
}
