package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.learningpath.*;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.learning.LearningPath;
import com.threadqa.lms.model.learning.LearningPathItem;
import com.threadqa.lms.model.learning.UserLearningPathProgress;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.learning.UserLearningPathProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningPathMapper {
    
    @Autowired
    private UserLearningPathProgressRepository progressRepository;
    
    public LearningPath toEntity(LearningPathRequest request, User createdBy) {
        return LearningPath.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .slug(request.getSlug())
                .difficultyLevel(request.getDifficultyLevel())
                .estimatedHours(request.getEstimatedHours())
                .published(request.isPublished())
                .featured(request.isFeatured())
                .createdBy(createdBy)
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(request.getTags())
                .build();
    }
    
    public LearningPathResponse toResponse(LearningPath learningPath) {
        Long pathId = learningPath.getId();
        Long enrollmentCount = progressRepository.countEnrollmentsByLearningPath(pathId);
        Long completionCount = progressRepository.countCompletionsByLearningPath(pathId);
        Double averageProgress = progressRepository.getAverageProgressForLearningPath(pathId);
        
        return LearningPathResponse.builder()
                .id(learningPath.getId())
                .title(learningPath.getTitle())
                .description(learningPath.getDescription())
                .slug(learningPath.getSlug())
                .difficultyLevel(learningPath.getDifficultyLevel())
                .estimatedHours(learningPath.getEstimatedHours())
                .published(learningPath.isPublished())
                .featured(learningPath.isFeatured())
                .createdById(learningPath.getCreatedBy().getId())
                .createdByName(learningPath.getCreatedBy().getFullName())
                .createdAt(learningPath.getCreatedAt())
                .updatedAt(learningPath.getUpdatedAt())
                .thumbnailUrl(learningPath.getThumbnailUrl())
                .tags(learningPath.getTags())
                .enrollmentCount(enrollmentCount != null ? enrollmentCount.intValue() : 0)
                .completionCount(completionCount != null ? completionCount.intValue() : 0)
                .averageProgress(averageProgress != null ? averageProgress : 0.0)
                .build();
    }
    
    public LearningPathResponse toResponseWithItems(LearningPath learningPath, List<LearningPathItem> items) {
        LearningPathResponse response = toResponse(learningPath);
        response.setItems(items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList()));
        return response;
    }
    
    public LearningPathItem toItemEntity(LearningPathItemRequest request, LearningPath learningPath, Course course) {
        return LearningPathItem.builder()
                .learningPath(learningPath)
                .course(course)
                .position(request.getPosition())
                .required(request.isRequired())
                .notes(request.getNotes())
                .build();
    }
    
    public LearningPathItemResponse toItemResponse(LearningPathItem item) {
        Course course = item.getCourse();
        return LearningPathItemResponse.builder()
                .id(item.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseSlug(course.getSlug())
                .courseThumbnailUrl(course.getThumbnailUrl())
                .position(item.getPosition())
                .required(item.isRequired())
                .notes(item.getNotes())
                .durationHours(course.getDurationHours())
                .build();
    }
    
    public UserLearningPathProgressResponse toProgressResponse(UserLearningPathProgress progress, int totalItems, int completedItems) {
        return UserLearningPathProgressResponse.builder()
                .id(progress.getId())
                .learningPathId(progress.getLearningPath().getId())
                .learningPathTitle(progress.getLearningPath().getTitle())
                .learningPathSlug(progress.getLearningPath().getSlug())
                .thumbnailUrl(progress.getLearningPath().getThumbnailUrl())
                .progressPercentage(progress.getProgressPercentage())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .lastActivityAt(progress.getLastActivityAt())
                .completed(progress.isCompleted())
                .totalItems(totalItems)
                .completedItems(completedItems)
                .build();
    }
    
    public UserLearningPathProgressResponse toDetailedProgressResponse(UserLearningPathProgress progress, 
                                                                      int totalItems, 
                                                                      int completedItems,
                                                                      List<LearningPathItem> items) {
        UserLearningPathProgressResponse response = toProgressResponse(progress, totalItems, completedItems);
        LearningPathResponse pathResponse = toResponseWithItems(progress.getLearningPath(), items);
        response.setLearningPath(pathResponse);
        return response;
    }
}
