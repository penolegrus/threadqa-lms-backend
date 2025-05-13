package com.threadqa.lms.dto.assessment.quiz;

import com.threadqa.lms.model.assessment.QuizQuestion;
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
public class QuizQuestionRequest {

    private Long id;

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must be less than 1000 characters")
    private String questionText;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    @NotNull(message = "Question type is required")
    private QuizQuestion.QuestionType questionType;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be at least 0")
    private Integer orderIndex;

    @Valid
    private List<QuizOptionRequest> options = new ArrayList<>();
}
