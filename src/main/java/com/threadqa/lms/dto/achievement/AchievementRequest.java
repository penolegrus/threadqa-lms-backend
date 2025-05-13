package com.threadqa.lms.dto.achievement;

import com.threadqa.lms.model.achievement.Achievement;
import jakarta.validation.constraints.Min;
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
public class AchievementRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String iconUrl;

    @NotNull(message = "Achievement type is required")
    private Achievement.AchievementType type;

    @NotNull(message = "Threshold is required")
    @Min(value = 1, message = "Threshold must be at least 1")
    private Integer threshold;

    @NotNull(message = "XP reward is required")
    @Min(value = 0, message = "XP reward must be at least 0")
    private Integer xpReward;
}
