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
public class SystemAnalyticsResponse {
    
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer studentCount;
    private Integer instructorCount;
    private Integer totalCourses;
    private Integer publishedCourses;
    private Integer totalEnrollments;
    private Integer completedCourses;
    private Double systemWideCompletionRate;
    private Double averageRating;
    private Map<String, Integer> usersByMonth;
    private Map<String, Integer> coursesByMonth;
    private Map<String, Integer> enrollmentsByMonth;
    private List<CategoryStatsDTO> categoryStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatsDTO {
        private Long categoryId;
        private String categoryName;
        private Integer courseCount;
        private Integer enrollmentCount;
        private Double averageRating;
    }
}
