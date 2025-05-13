package com.threadqa.lms.dto.assessment.quiz;

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
public class QuizQuestionAnswerResponse {

    private Long id;
    private Long questionId;
    private String questionText;
    private String textAnswer;
    private Integer pointsEarned;
    private Integer maxPoints;
    private List<QuizOptionResponse> selectedOptions = new ArrayList<>();
    private List<QuizOptionResponse> correctOptions = new ArrayList<>();
    private Boolean isCorrect;
}
