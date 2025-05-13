package com.threadqa.lms.dto.assessment.quiz;

import com.threadqa.lms.model.assessment.QuizQuestion;
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
public class QuizQuestionResponse {

    private Long id;
    private String questionText;
    private Integer points;
    private QuizQuestion.QuestionType questionType;
    private Integer orderIndex;
    private List<QuizOptionResponse> options = new ArrayList<>();
}
