package com.threadqa.lms.dto.assessment.test;
import com.threadqa.lms.dto.assessment.TestOptionResponse;
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
public class TestQuestionResponse {

    private Long id;
    private Long testId;
    private String question;
    private TestQuestion.QuestionType questionType;
    private Integer points;
    private Integer orderIndex;
    private String codeSnippet;
    private String codeLanguage;
    private List<TestOptionResponse> options;
    private List<TestMatchingPairResponse> matchingPairs;
}