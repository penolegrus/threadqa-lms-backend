package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.course.CourseReviewResponse;
import com.threadqa.lms.model.course.CourseReview;
import org.springframework.stereotype.Component;

@Component
public class CourseReviewMapper {

    public CourseReviewResponse toCourseReviewResponse(CourseReview review) {
        if (review == null) {
            return null;
        }

        return CourseReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .userProfilePicture(review.getUser().getProfilePicture())
                .courseId(review.getCourse().getId())
                .courseTitle(review.getCourse().getTitle())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
