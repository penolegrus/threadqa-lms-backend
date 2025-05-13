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
public class UserAnalyticsResponse {
    
    private Long userId;
    private String userName;
    private Integer enrolledCourses;
    private Integer completedCourses;
    private Double completionRate;
    private Double averageProgress;
    private Integer testsTaken;
    private Double averageTestScore;
    private Integer earnedAchievements;
    private Map<String, Double> progressByMonth;
    private Map<String, Integer> courseEnrollmentsByMonth;
    private Map<String, Integer> courseCompletionsByMonth;
    private List<CourseProgressDTO> courseProgress;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseProgressDTO {
        private Long courseId;
        private String courseTitle;
        private Double progress;
        private Boolean isCompleted;
        private Integer lastActiveDate;
        private Integer totalTimeSpent;
    }
}
