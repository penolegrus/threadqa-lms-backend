package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Streak;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Integer currentStreak;
    private Integer longestStreak;
    private ZonedDateTime lastActivityDate;
    private Streak.StreakType streakType;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
