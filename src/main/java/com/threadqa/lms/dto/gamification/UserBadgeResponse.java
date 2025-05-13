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
public class UserBadgeResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long badgeId;
    private String badgeName;
    private String badgeDescription;
    private String badgeImageUrl;
    private String badgeType;
    private Boolean isDisplayed;
    private ZonedDateTime awardedAt;
}
