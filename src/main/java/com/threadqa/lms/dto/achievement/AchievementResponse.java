package com.threadqa.lms.dto.achievement;

import com.threadqa.lms.model.achievement.Achievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {

    private Long id;
    private String name;
    private String title;
    private String description;
    private String iconUrl;
    private Achievement.AchievementType type;
    private Integer threshold;
    private Integer xpReward;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Integer completedCount;
}
