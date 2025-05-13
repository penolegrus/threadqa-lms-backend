package com.threadqa.lms.dto.learningpath;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLearningPathProgressResponse {
    
    private Long id;
    private Long learningPathId;
    private String learningPathTitle;
    private String learningPathSlug;
    private String thumbnailUrl;
    private Integer progressPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastActivityAt;
    private boolean completed;
    private Integer totalItems;
    private Integer completedItems;
    private LearningPathResponse learningPath;
}
