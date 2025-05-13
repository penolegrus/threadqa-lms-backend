package com.threadqa.lms.dto.homework;

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
public class HomeworkRequirementRequest {

    private Long id; // Может быть null для новых требований

    @NotBlank(message = "Description is required")
    private String description;

    private Integer orderIndex;

    @NotNull(message = "Points are required")
    private Integer points;
}
