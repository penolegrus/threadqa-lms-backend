package com.threadqa.lms.dto.learningpath;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItemResponse {
    
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseSlug;
    private String courseThumbnailUrl;
    private Integer position;
    private boolean required;
    private String notes;
    private Integer durationHours;
}
