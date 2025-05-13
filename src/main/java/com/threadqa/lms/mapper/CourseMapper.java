package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.course.CategoryDTO;
import com.threadqa.lms.dto.course.CourseResponse;
import com.threadqa.lms.model.course.Category;
import com.threadqa.lms.model.course.Course;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    public CourseResponse toCourseResponse(
            Course course,
            Integer topicCount,
            Integer enrollmentCount,
            Double averageRating,
            Integer reviewCount,
            Boolean isEnrolled,
            Boolean isCompleted,
            Double progress) {

        if (course == null) {
            return null;
        }

        List<CategoryDTO> categories = course.getCategories().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .coverImage(course.getCoverImage())
                .isPublished(course.getIsPublished())
                .isFeatured(course.getIsFeatured())
                .price(course.getPrice())
                .discountPrice(course.getDiscountPrice())
                .discountStartDate(course.getDiscountStartDate())
                .discountEndDate(course.getDiscountEndDate())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .publishedAt(course.getPublishedAt())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName())
                .categories(categories)
                .durationHours(course.getDurationHours())
                .level(course.getLevel())
                .language(course.getLanguage())
                .prerequisites(course.getPrerequisites())
                .learningObjectives(course.getLearningObjectives())
                .targetAudience(course.getTargetAudience())
                .topicCount(topicCount)
                .enrollmentCount(enrollmentCount)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .isEnrolled(isEnrolled)
                .isCompleted(isCompleted)
                .progress(progress)
                .build();
    }

    private CategoryDTO toCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}