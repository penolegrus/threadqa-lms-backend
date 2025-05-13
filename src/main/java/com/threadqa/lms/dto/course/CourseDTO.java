package com.threadqa.lms.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private Boolean isPublished;
    private Boolean isFeatured;
    private Double price;
    private Double discountPrice;
    private ZonedDateTime discountStartDate;
    private ZonedDateTime discountEndDate;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime publishedAt;
    private Long instructorId;
    private String instructorName;
    private List<CategoryDTO> categories;
    private Integer durationHours;
    private String level;
    private String language;
    private String prerequisites;
    private String learningObjectives;
    private String targetAudience;
}