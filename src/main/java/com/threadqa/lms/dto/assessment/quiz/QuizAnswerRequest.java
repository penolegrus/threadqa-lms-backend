package com.threadqa.lms.dto.assessment.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class QuizAnswerRequest {

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @Valid
    private List<QuizQuestionAnswerRequest> questionAnswers = new ArrayList<>();
}
