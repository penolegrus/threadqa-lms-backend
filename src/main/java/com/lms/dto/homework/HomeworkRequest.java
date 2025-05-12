package com.lms.dto.homework;

import jakarta.validation.constraints.Future;
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

    @Future(message = "Due date must be in the future")
    private ZonedDateTime dueDate;

    private Integer maxPoints;

    private List<HomeworkRequirementRequest> requirements;
}