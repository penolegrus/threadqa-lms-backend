package com.threadqa.lms.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevelResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long levelId;
    private String levelName;
    private Integer levelNumber;
    private Integer currentPoints;
    private Integer pointsToNextLevel;
    private ZonedDateTime achievedAt;
    private ZonedDateTime updatedAt;
}
