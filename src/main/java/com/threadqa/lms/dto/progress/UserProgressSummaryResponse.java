package com.threadqa.lms.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressSummaryResponse {

    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private Integer completedItems;
    private Integer totalItems;
    private Double completionPercentage;
    private Long totalTimeSpentSeconds;
    private Integer totalPageViews;
    private Integer totalInteractions;
    private Integer totalComments;
    private Integer totalQuestionsAsked;
    private Integer totalLogins;
    private String lastActivity;
    private String lastActivityDate;
}
