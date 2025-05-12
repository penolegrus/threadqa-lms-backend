package com.lms.dto.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class TestSubmissionRequest {

    @NotNull(message = "Test ID is required")
    private Long testId;

    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<TestQuestionAnswerRequest> answers;

    @NotNull(message = "Time spent is required")
    private Long timeSpentSeconds;
}