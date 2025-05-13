package com.threadqa.lms.dto.assessment.test;

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
public class TestAnswerRequest {
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    private List<Long> selectedOptionIds;
    
    private String textAnswer;
    
    private String codeAnswer;
}
