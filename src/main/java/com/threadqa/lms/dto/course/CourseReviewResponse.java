package com.threadqa.lms.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userProfilePicture;
    private Long courseId;
    private String courseTitle;
    private Integer rating;
    private String comment;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
