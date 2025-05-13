package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Badge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Badge.BadgeType badgeType;
    private Integer threshold;
    private Integer pointsReward;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
