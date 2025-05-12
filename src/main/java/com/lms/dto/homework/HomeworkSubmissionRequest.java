package com.lms.dto.homework;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkSubmissionRequest {

    @NotNull(message = "Homework ID is required")
    private Long homeworkId;

    @NotBlank(message = "Submission text is required")
    private String submissionText;

    private String githubUrl;

    private String additionalNotes;
}