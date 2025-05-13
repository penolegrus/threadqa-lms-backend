package com.threadqa.lms.dto.assessment.test;
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
public class TestRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    private Integer timeLimitMinutes;

    @NotNull(message = "Passing score is required")
    private Integer passingScore;

    private Integer maxAttempts;

    @NotNull(message = "Randomized flag is required")
    private Boolean isRandomized;

    @NotNull(message = "Published flag is required")
    private Boolean isPublished;

    private List<TestQuestionRequest> questions;
}
