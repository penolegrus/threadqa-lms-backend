package com.threadqa.lms.dto.progress;

import com.threadqa.lms.model.progress.UserActivity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {

    private Long id;
    private Long userId;
    private String userName;
    private UserActivity.ActivityType activityType;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String description;
    private String ipAddress;
    private String userAgent;
    private ZonedDateTime createdAt;
}
