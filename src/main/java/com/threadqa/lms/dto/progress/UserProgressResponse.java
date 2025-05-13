package com.threadqa.lms.dto.progress;

import com.threadqa.lms.model.progress.UserProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private Long topicId;
    private String topicTitle;
    private UserProgress.ContentType contentType;
    private Long contentId;
    private String contentTitle;
    private Boolean isCompleted;
    private Double progressPercentage;
    private Long timeSpentSeconds;
    private ZonedDateTime lastAccessedAt;
    private ZonedDateTime completedAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
