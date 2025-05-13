package com.threadqa.lms.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEngagementResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private ZonedDateTime engagementDate;
    private Long sessionDurationSeconds;
    private Integer pageViews;
    private Integer interactions;
    private Integer comments;
    private Integer questionsAsked;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
