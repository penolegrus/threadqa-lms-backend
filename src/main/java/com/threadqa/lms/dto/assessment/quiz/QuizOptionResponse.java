package com.threadqa.lms.dto.assessment.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionResponse {

    private Long id;
    private String optionText;
    private Boolean isCorrect;
    private Integer orderIndex;
    private String matchingText;
}
