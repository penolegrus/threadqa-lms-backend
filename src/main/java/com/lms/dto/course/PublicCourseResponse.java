package com.lms.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCourseResponse {

    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String category;
    private String imageUrl;
    private String coverImageUrl;
    private InstructorDTO instructor;
    private Double rating;
    private Integer reviewsCount;
    private Integer studentsCount;
    private Integer lessonsCount;
    private String duration;
    private String level;
    private Set<String> skills;
    private List<SyllabusItemDTO> syllabus;
    private List<FaqDTO> faqs;
    private List<TestimonialDTO> testimonials;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorDTO {
        private Long id;
        private String name;
        private String bio;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyllabusItemDTO {
        private Long id;
        private String title;
        private String description;
        private Integer duration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaqDTO {
        private String question;
        private String answer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestimonialDTO {
        private Long id;
        private String studentName;
        private String avatar;
        private Integer rating;
        private String text;
        private LocalDate date;
    }
}