package com.threadqa.lms.dto.progress;

import com.threadqa.lms.model.progress.UserProgress;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressRequest {

    @NotNull
    private Long courseId;

    @NotNull
    private Long topicId;

    @NotNull
    private UserProgress.ContentType contentType;

    @NotNull
    private Long contentId;

    private Boolean isCompleted;

    private Double progressPercentage;

    private Long timeSpentSeconds;
}
