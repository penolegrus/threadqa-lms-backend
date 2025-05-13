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
public class QuizResponse {

    private Long id;
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer passingScore;
    private Boolean isActive;
    private Boolean shuffleQuestions;
    private Boolean showAnswers;
    private Long topicId;
    private String topicTitle;
    private Long courseId;
    private String courseTitle;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuizQuestionResponse> questions = new ArrayList<>();
    
    // Statistics
    private Integer totalAttempts;
    private Integer passedAttempts;
    private Double averageScore;
}
