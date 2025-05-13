package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.progress.UserActivityResponse;
import com.threadqa.lms.dto.progress.UserEngagementResponse;
import com.threadqa.lms.dto.progress.UserProgressResponse;
import com.threadqa.lms.model.progress.UserActivity;
import com.threadqa.lms.model.progress.UserEngagement;
import com.threadqa.lms.model.progress.UserProgress;
import org.springframework.stereotype.Component;

@Component
public class ProgressMapper {

    public UserProgressResponse toUserProgressResponse(UserProgress userProgress) {
        if (userProgress == null) {
            return null;
        }

        return UserProgressResponse.builder()
                .id(userProgress.getId())
                .userId(userProgress.getUser().getId())
                .userName(userProgress.getUser().getFirstName() + " " + userProgress.getUser().getLastName())
                .courseId(userProgress.getCourse().getId())
                .courseTitle(userProgress.getCourse().getTitle())
                .topicId(userProgress.getTopic().getId())
                .topicTitle(userProgress.getTopic().getTitle())
                .contentType(userProgress.getContentType())
                .contentId(userProgress.getContentId())
                .contentTitle(getContentTitle(userProgress))
                .isCompleted(userProgress.getIsCompleted())
                .progressPercentage(userProgress.getProgressPercentage())
                .timeSpentSeconds(userProgress.getTimeSpentSeconds())
                .lastAccessedAt(userProgress.getLastAccessedAt())
                .completedAt(userProgress.getCompletedAt())
                .createdAt(userProgress.getCreatedAt())
                .updatedAt(userProgress.getUpdatedAt())
                .build();
    }

    public UserActivityResponse toUserActivityResponse(UserActivity userActivity) {
        if (userActivity == null) {
            return null;
        }

        return UserActivityResponse.builder()
                .id(userActivity.getId())
                .userId(userActivity.getUser().getId())
                .userName(userActivity.getUser().getFirstName() + " " + userActivity.getUser().getLastName())
                .activityType(userActivity.getActivityType())
                .entityType(userActivity.getEntityType())
                .entityId(userActivity.getEntityId())
                .entityName(getEntityName(userActivity))
                .description(userActivity.getDescription())
                .ipAddress(userActivity.getIpAddress())
                .userAgent(userActivity.getUserAgent())
                .createdAt(userActivity.getCreatedAt())
                .build();
    }

    public UserEngagementResponse toUserEngagementResponse(UserEngagement userEngagement) {
        if (userEngagement == null) {
            return null;
        }

        return UserEngagementResponse.builder()
                .id(userEngagement.getId())
                .userId(userEngagement.getUser().getId())
                .userName(userEngagement.getUser().getFirstName() + " " + userEngagement.getUser().getLastName())
                .courseId(userEngagement.getCourse() != null ? userEngagement.getCourse().getId() : null)
                .courseTitle(userEngagement.getCourse() != null ? userEngagement.getCourse().getTitle() : null)
                .engagementDate(userEngagement.getEngagementDate())
                .sessionDurationSeconds(userEngagement.getSessionDurationSeconds())
                .pageViews(userEngagement.getPageViews())
                .interactions(userEngagement.getInteractions())
                .comments(userEngagement.getComments())
                .questionsAsked(userEngagement.getQuestionsAsked())
                .createdAt(userEngagement.getCreatedAt())
                .updatedAt(userEngagement.getUpdatedAt())
                .build();
    }

    // Вспомогательные методы для получения названий контента и сущностей
    private String getContentTitle(UserProgress userProgress) {
        // В реальном приложении здесь будет логика получения названия контента
        // в зависимости от типа контента и его ID
        return "Content #" + userProgress.getContentId();
    }

    private String getEntityName(UserActivity userActivity) {
        // В реальном приложении здесь будет логика получения названия сущности
        // в зависимости от типа сущности и ее ID
        return userActivity.getEntityType() + " #" + userActivity.getEntityId();
    }
}
