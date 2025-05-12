package com.lms.dto.test;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    private Long selectedOptionId;

    private String textAnswer;

    private String codeAnswer;
}