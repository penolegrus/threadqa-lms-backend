package com.threadqa.lms.dto.assessment.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Time limit is required")
    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimit;

    @NotNull(message = "Passing score is required")
    @Min(value = 0, message = "Passing score must be at least 0")
    private Integer passingScore;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @NotNull(message = "Shuffle questions setting is required")
    private Boolean shuffleQuestions;

    @NotNull(message = "Show answers setting is required")
    private Boolean showAnswers;

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    @Valid
    private List<QuizQuestionRequest> questions = new ArrayList<>();
}
