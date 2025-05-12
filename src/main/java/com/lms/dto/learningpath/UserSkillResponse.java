package com.lms.dto.learningpath;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillResponse {

    private Long id;
    private Long userId;
    private Long skillId;
    private String skillName;
    private String skillDescription;
    private Integer skillLevel;
    private Integer proficiencyLevel;
    private Boolean isCompleted;
    private ZonedDateTime completedAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}