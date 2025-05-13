package com.threadqa.lms.dto.assessment.quiz;

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
public class QuizQuestionAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @Size(max = 1000, message = "Text answer must be less than 1000 characters")
    private String textAnswer;

    private List<Long> selectedOptionIds = new ArrayList<>();
}
