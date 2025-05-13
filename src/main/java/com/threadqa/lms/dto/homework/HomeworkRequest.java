package com.threadqa.lms.dto.homework;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class HomeworkRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    @NotNull(message = "Max score is required")
    private Integer maxScore;

    private ZonedDateTime dueDate;

    @NotNull(message = "Published status is required")
    private Boolean isPublished;

    @Valid
    private List<HomeworkRequirementRequest> requirements;
}
