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
public class LevelResponse {

    private Long id;
    private String name;
    private Integer levelNumber;
    private Integer pointsRequired;
    private String description;
    private String imageUrl;
    private String benefits;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
