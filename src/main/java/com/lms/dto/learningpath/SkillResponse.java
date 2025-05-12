package com.lms.dto.learningpath;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private Long id;
    private String name;
    private String description;
    private Integer skillLevel;
    private Long learningPathId;
    private List<SkillDependencyResponse> prerequisites;
    private List<Long> courseIds;
    private List<String> courseNames;
}