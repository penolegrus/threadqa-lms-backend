package com.threadqa.lms.dto.homework;

import com.threadqa.lms.model.homework.HomeworkSubmission;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkReviewRequest {

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must be at most 100")
    private Integer score;

    private String feedback;

    @NotNull(message = "Status is required")
    private HomeworkSubmission.SubmissionStatus status;
}
