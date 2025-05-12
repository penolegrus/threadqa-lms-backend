package com.lms.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private String coverImageUrl;
    private InstructorDTO instructor;
    private Double rating;
    private Integer studentsCount;
    private Integer lessonsCount;
    private String duration;
    private String level;
    private Set<String> skills;
    private boolean isPublished;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorDTO {
        private Long id;
        private String name;
        private String avatar;
    }
}