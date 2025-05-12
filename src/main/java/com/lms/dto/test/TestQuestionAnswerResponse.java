package com.lms.dto.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionAnswerResponse {

    private Long id;
    private Long questionId;
    private String questionText;
    private String questionType;
    private Long selectedOptionId;
    private String selectedOptionText;
    private String textAnswer;
    private String codeAnswer;
    private Boolean isCorrect;
    private Integer score;
    private String feedback;
    private String correctAnswer;
}