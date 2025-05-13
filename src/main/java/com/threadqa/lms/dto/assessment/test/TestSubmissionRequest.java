package com.threadqa.lms.dto.assessment.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionRequest {
    
    @NotNull(message = "Start time is required")
    private ZonedDateTime startedAt;
    
    @NotEmpty(message = "Answers are required")
    @Valid
    private List<TestAnswerRequest> answers;
}
