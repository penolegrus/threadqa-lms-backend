package com.threadqa.lms.dto.learningpath;

import com.threadqa.lms.model.learning.LearningPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {
    
    private Long id;
    private String title;
    private String description;
    private String slug;
    private LearningPath.DifficultyLevel difficultyLevel;
    private Integer estimatedHours;
    private boolean published;
    private boolean featured;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LearningPathItemResponse> items;
    private String thumbnailUrl;
    private String tags;
    private Integer enrollmentCount;
    private Integer completionCount;
    private Double averageProgress;
}
