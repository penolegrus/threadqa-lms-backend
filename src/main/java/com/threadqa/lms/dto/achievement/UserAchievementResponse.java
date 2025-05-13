package com.threadqa.lms.dto.achievement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long achievementId;
    private String achievementName;
    private String achievementTitle;
    private String achievementDescription;
    private String achievementIconUrl;
    private Integer xpReward;
    private Integer progress;
    private Integer threshold;
    private Boolean isCompleted;
    private ZonedDateTime earnedAt;
}
