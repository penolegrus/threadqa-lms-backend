package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Leaderboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    private Long id;
    private String name;
    private String description;
    private Leaderboard.LeaderboardType leaderboardType;
    private Leaderboard.TimePeriod timePeriod;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
