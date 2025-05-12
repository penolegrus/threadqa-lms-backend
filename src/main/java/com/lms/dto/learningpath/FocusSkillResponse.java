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
public class FocusSkillResponse {

    private Long id;
    private Long userId;
    private Long skillId;
    private String skillName;
    private Integer priority;
    private ZonedDateTime createdAt;
}