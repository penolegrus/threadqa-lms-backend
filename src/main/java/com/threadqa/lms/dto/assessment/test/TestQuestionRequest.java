package com.threadqa.lms.dto.assessment.test;
import com.threadqa.lms.dto.assessment.TestOptionRequest;
import com.threadqa.lms.model.assessment.TestQuestion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionRequest {

    private Long id; // Может быть null для новых вопросов

    @NotBlank(message = "Question text is required")
    private String question;

    @NotNull(message = "Question type is required")
    private TestQuestion.QuestionType questionType;

    @NotNull(message = "Points are required")
    private Integer points;

    private Integer orderIndex;

    private String codeSnippet;

    private String codeLanguage;

    private List<TestOptionRequest> options;

    private List<TestMatchingPairRequest> matchingPairs;
}
