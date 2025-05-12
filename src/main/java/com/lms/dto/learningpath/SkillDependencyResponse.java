package com.lms.dto.learningpath;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDependencyResponse {

    private Long skillId;
    private String skillName;
    private Boolean isRequired;
}