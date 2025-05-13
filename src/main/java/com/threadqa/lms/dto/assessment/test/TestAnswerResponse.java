package com.threadqa.lms.dto.assessment.test;

import com.threadqa.lms.model.assessment.TestQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAnswerResponse {
    
    private Long id;
    private Long submissionId;
    private Long questionId;
    private String questionText;
    private TestQuestion.QuestionType questionType;
    private List<Long> selectedOptionIds;
    private String textAnswer;
    private String codeAnswer;
    private Integer pointsEarned;
    private Boolean isCorrect;
    private String feedback;
}
