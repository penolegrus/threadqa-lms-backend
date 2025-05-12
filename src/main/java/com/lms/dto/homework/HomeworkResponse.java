package com.lms.dto.homework;

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
public class HomeworkResponse {

    private Long id;
    private String title;
    private String description;
    private Long topicId;
    private String topicTitle;
    private Long courseId;
    private String courseTitle;
    private ZonedDateTime dueDate;
    private Integer maxPoints;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<HomeworkRequirementResponse> requirements;
    private Integer submissionCount;
}