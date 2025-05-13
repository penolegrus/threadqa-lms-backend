package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.gamification.*;
import com.threadqa.lms.model.gamification.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GamificationMapper {

    public PointResponse toPointResponse(Point point) {
        if (point == null) {
            return null;
        }

        return PointResponse.builder()
                .id(point.getId())
                .userId(point.getUser().getId())
                .userName(point.getUser().getFirstName() + " " + point.getUser().getLastName())
                .amount(point.getAmount())
                .pointType(point.getPointType())
                .description(point.getDescription())
                .entityType(point.getEntityType())
                .entityId(point.getEntityId())
                .createdAt(point.getCreatedAt())
                .build();
    }

    public BadgeResponse toBadgeResponse(Badge badge) {
        if (badge == null) {
            return null;
        }

        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .imageUrl(badge.getImageUrl())
                .badgeType(badge.getBadgeType())
                .threshold(badge.getThreshold())
                .pointsReward(badge.getPointsReward())
                .isActive(badge.getIsActive())
                .createdAt(badge.getCreatedAt())
                .updatedAt(badge.getUpdatedAt())
                .build();
    }

    public UserBadgeResponse toUserBadgeResponse(UserBadge userBadge) {
        if (userBadge == null) {
            return null;
        }

        return UserBadgeResponse.builder()
                .id(userBadge.getId())
                .userId(userBadge.getUser().getId())
                .userName(userBadge.getUser().getFirstName() + " " + userBadge.getUser().getLastName())
                .badgeId(userBadge.getBadge().getId())
                .badgeName(userBadge.getBadge().getName())
                .badgeDescription(userBadge.getBadge().getDescription())
                .badgeImageUrl(userBadge.getBadge().getImageUrl())
                .badgeType(userBadge.getBadge().getBadgeType().toString())
                .isDisplayed(userBadge.getIsDisplayed())
                .awardedAt(userBadge.getAwardedAt())
                .build();
    }

    public LevelResponse toLevelResponse(Level level) {
        if (level == null) {
            return null;
        }

        return LevelResponse.builder()
                .id(level.getId())
                .name(level.getName())
                .levelNumber(level.getLevelNumber())
                .pointsRequired(level.getPointsRequired())
                .description(level.getDescription())
                .imageUrl(level.getImageUrl())
                .benefits(level.getBenefits())
                .createdAt(level.getCreatedAt())
                .updatedAt(level.getUpdatedAt())
                .build();
    }

    public UserLevelResponse toUserLevelResponse(UserLevel userLevel) {
        if (userLevel == null) {
            return null;
        }

        return UserLevelResponse.builder()
                .id(userLevel.getId())
                .userId(userLevel.getUser().getId())
                .userName(userLevel.getUser().getFirstName() + " " + userLevel.getUser().getLastName())
                .levelId(userLevel.getLevel().getId())
                .levelName(userLevel.getLevel().getName())
                .levelNumber(userLevel.getLevel().getLevelNumber())
                .currentPoints(userLevel.getCurrentPoints())
                .pointsToNextLevel(userLevel.getPointsToNextLevel())
                .achievedAt(userLevel.getAchievedAt())
                .updatedAt(userLevel.getUpdatedAt())
                .build();
    }

    public LeaderboardResponse toLeaderboardResponse(Leaderboard leaderboard) {
        if (leaderboard == null) {
            return null;
        }

        return LeaderboardResponse.builder()
                .id(leaderboard.getId())
                .name(leaderboard.getName())
                .description(leaderboard.getDescription())
                .leaderboardType(leaderboard.getLeaderboardType())
                .timePeriod(leaderboard.getTimePeriod())
                .isActive(leaderboard.getIsActive())
                .createdAt(leaderboard.getCreatedAt())
                .updatedAt(leaderboard.getUpdatedAt())
                .build();
    }

    public LeaderboardEntryResponse toLeaderboardEntryResponse(LeaderboardEntry entry) {
        if (entry == null) {
            return null;
        }

        return LeaderboardEntryResponse.builder()
                .id(entry.getId())
                .leaderboardId(entry.getLeaderboard().getId())
                .leaderboardName(entry.getLeaderboard().getName())
                .userId(entry.getUser().getId())
                .userName(entry.getUser().getFirstName() + " " + entry.getUser().getLastName())
                .userProfileImage(entry.getUser().getProfileImageUrl())
                .score(entry.getScore())
                .rank(entry.getRank())
                .periodStart(entry.getPeriodStart())
                .periodEnd(entry.getPeriodEnd())
                .build();
    }

    public StreakResponse toStreakResponse(Streak streak) {
        if (streak == null) {
            return null;
        }

        return StreakResponse.builder()
                .id(streak.getId())
                .userId(streak.getUser().getId())
                .userName(streak.getUser().getFirstName() + " " + streak.getUser().getLastName())
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastActivityDate(streak.getLastActivityDate())
                .streakType(streak.getStreakType())
                .createdAt(streak.getCreatedAt())
                .updatedAt(streak.getUpdatedAt())
                .build();
    }

    public GamificationSummaryResponse toGamificationSummaryResponse(
            Long userId, String userName, Integer totalPoints, 
            UserLevel userLevel, List<UserBadge> recentBadges, 
            Streak loginStreak, List<LeaderboardEntry> leaderboardEntries) {
        
        List<UserBadgeResponse> badgeResponses = recentBadges.stream()
                .map(this::toUserBadgeResponse)
                .collect(Collectors.toList());
        
        List<LeaderboardEntryResponse> leaderboardResponses = leaderboardEntries.stream()
                .map(this::toLeaderboardEntryResponse)
                .collect(Collectors.toList());
        
        return GamificationSummaryResponse.builder()
                .userId(userId)
                .userName(userName)
                .totalPoints(totalPoints)
                .level(userLevel != null ? userLevel.getLevel().getLevelNumber() : 0)
                .levelName(userLevel != null ? userLevel.getLevel().getName() : "Новичок")
                .pointsToNextLevel(userLevel != null ? userLevel.getPointsToNextLevel() : 0)
                .badgesCount(recentBadges.size())
                .recentBadges(badgeResponses)
                .currentLoginStreak(loginStreak != null ? loginStreak.getCurrentStreak() : 0)
                .longestLoginStreak(loginStreak != null ? loginStreak.getLongestStreak() : 0)
                .leaderboardPositions(leaderboardResponses)
                .build();
    }
}
