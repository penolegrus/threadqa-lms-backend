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
public class LeaderboardEntryResponse {

    private Long id;
    private Long leaderboardId;
    private String leaderboardName;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private Integer score;
    private Integer rank;
    private ZonedDateTime periodStart;
    private ZonedDateTime periodEnd;
}
