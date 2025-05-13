package com.threadqa.lms.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamificationSummaryResponse {

    private Long userId;
    private String userName;
    private Integer totalPoints;
    private Integer level;
    private String levelName;
    private Integer pointsToNextLevel;
    private Integer badgesCount;
    private List<UserBadgeResponse> recentBadges;
    private Integer currentLoginStreak;
    private Integer longestLoginStreak;
    private List<LeaderboardEntryResponse> leaderboardPositions;
}
