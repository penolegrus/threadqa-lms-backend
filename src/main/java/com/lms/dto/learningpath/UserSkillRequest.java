package com.lms.dto.learningpath;

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
public class UserSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Proficiency level is required")
    @Min(value = 0, message = "Proficiency level must be at least 0")
    @Max(value = 10, message = "Proficiency level must be at most 10")
    private Integer proficiencyLevel;

    @NotNull(message = "Completion status is required")
    private Boolean isCompleted;
}