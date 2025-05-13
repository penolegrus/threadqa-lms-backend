package com.threadqa.lms.dto.assessment.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStatisticsResponse {

    private Long quizId;
    private String quizTitle;
    private Integer totalAttempts;
    private Integer completedAttempts;
    private Integer passedAttempts;
    private Double passRate;
    private Double averageScore;
    private Integer averageTimeMinutes;
    private List<QuestionStatistics> questionStatistics = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionStatistics {
        private Long questionId;
        private String questionText;
        private Double correctRate;
        private Integer totalAttempts;
        private Integer correctAttempts;
        private Map<String, Integer> optionDistribution;
    }
}
