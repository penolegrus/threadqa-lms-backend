package com.lms.dto.learningpath;

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
public class LearningPathResponse {

    private Long id;
    private String name;
    private String description;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<SkillResponse> skills;
}