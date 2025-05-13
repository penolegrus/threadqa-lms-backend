package com.threadqa.lms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAnalyticsResponse {
    
    private Long courseId;
    private String courseTitle;
    private Integer enrollmentCount;
    private Integer completionCount;
    private Double completionRate;
    private Double averageProgress;
    private Double averageRating;
    private Integer reviewCount;
    private Long visitorCount;
    private Map<String, Long> enrollmentsByMonth;
    private Map<String, Double> averageProgressByMonth;
    private Map<String, Integer> completionsByMonth;
    private List<LessonEngagementDTO> lessonEngagement;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonEngagementDTO {
        private Long topicId;
        private String topicTitle;
        private Integer views;
        private Double averageCompletionTime;
        private Double averageCompletionRate;
    }
}
