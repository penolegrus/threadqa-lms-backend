package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Badge;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeRequest {

    @NotBlank
    private String name;

    private String description;

    private String imageUrl;

    @NotNull
    private Badge.BadgeType badgeType;

    @NotNull
    @Positive
    private Integer threshold;

    private Integer pointsReward;

    @NotNull
    private Boolean isActive;
}
