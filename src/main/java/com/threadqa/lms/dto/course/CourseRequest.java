package com.threadqa.lms.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String coverImage;

    @NotNull(message = "Published status is required")
    private Boolean isPublished;

    private Boolean isFeatured;

    private Double price;

    private Double discountPrice;

    private ZonedDateTime discountStartDate;

    private ZonedDateTime discountEndDate;

    private Set<Long> categoryIds;

    private Integer durationHours;

    @NotBlank(message = "Level is required")
    private String level;

    @NotBlank(message = "Language is required")
    private String language;

    private String prerequisites;

    private String learningObjectives;

    private String targetAudience;
}
