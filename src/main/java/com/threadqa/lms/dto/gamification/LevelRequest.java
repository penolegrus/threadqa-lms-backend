package com.threadqa.lms.dto.gamification;

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
public class LevelRequest {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Integer levelNumber;

    @NotNull
    @Positive
    private Integer pointsRequired;

    private String description;

    private String imageUrl;

    private String benefits;
}
