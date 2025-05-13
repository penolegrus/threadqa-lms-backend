package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.achievement.AchievementResponse;
import com.threadqa.lms.dto.achievement.UserAchievementResponse;
import com.threadqa.lms.model.achievement.Achievement;
import com.threadqa.lms.model.user.UserAchievement;
import com.threadqa.lms.repository.user.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AchievementMapper {

    private final UserAchievementRepository userAchievementRepository;

    public AchievementResponse toAchievementResponse(Achievement achievement) {
        if (achievement == null) {
            return null;
        }

        // Подсчет количества пользователей, получивших достижение
        Long completedCount = userAchievementRepository.countCompletedByAchievementId(achievement.getId());

        return AchievementResponse.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .type(achievement.getType())
                .threshold(achievement.getThreshold())
                .xpReward(achievement.getXpReward())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
                .completedCount(completedCount != null ? completedCount.intValue() : 0)
                .build();
    }

    public UserAchievementResponse toUserAchievementResponse(UserAchievement userAchievement) {
        if (userAchievement == null) {
            return null;
        }

        return UserAchievementResponse.builder()
                .id(userAchievement.getId())
                .userId(userAchievement.getUser().getId())
                .userName(userAchievement.getUser().getFirstName() + " " + userAchievement.getUser().getLastName())
                .achievementId(userAchievement.getAchievement().getId())
                .achievementName(userAchievement.getAchievement().getName())
                .achievementTitle(userAchievement.getAchievement().getTitle())
                .achievementDescription(userAchievement.getAchievement().getDescription())
                .achievementIconUrl(userAchievement.getAchievement().getIconUrl())
                .xpReward(userAchievement.getAchievement().getXpReward())
                .progress(userAchievement.getProgress())
                .threshold(userAchievement.getAchievement().getThreshold())
                .isCompleted(userAchievement.getIsCompleted())
                .earnedAt(userAchievement.getEarnedAt())
                .build();
    }
}
