package com.threadqa.lms.dto.assessment.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionRequest {

    private Long id;

    @NotBlank(message = "Option text is required")
    @Size(max = 1000, message = "Option text must be less than 1000 characters")
    private String optionText;

    @NotNull(message = "Correct status is required")
    private Boolean isCorrect;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    // For matching questions
    private String matchingText;
}
