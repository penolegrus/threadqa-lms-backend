package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Leaderboard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Leaderboard.LeaderboardType leaderboardType;

    @NotNull
    private Leaderboard.TimePeriod timePeriod;

    @NotNull
    private Boolean isActive;
}
