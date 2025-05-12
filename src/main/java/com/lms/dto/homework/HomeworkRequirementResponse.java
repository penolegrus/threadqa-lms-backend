package com.lms.dto.homework;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkRequirementResponse {

    private Long id;
    private Long homeworkId;
    private String description;
    private Integer points;
    private Boolean isRequired;
}