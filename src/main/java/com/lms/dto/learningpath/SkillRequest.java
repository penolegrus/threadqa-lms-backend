package com.lms.dto.learningpath;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class SkillRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Skill level is required")
    @Min(value = 1, message = "Skill level must be at least 1")
    @Max(value = 10, message = "Skill level must be at most 10")
    private Integer skillLevel;

    @NotNull(message = "Learning path ID is required")
    private Long learningPathId;

    private List<Long> prerequisiteSkillIds;

    private List<Long> courseIds;
}