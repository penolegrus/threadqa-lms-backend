package com.threadqa.lms.dto.assessment.test;

import com.threadqa.lms.dto.assessment.TestQuestionResponse;
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
public class TestResponse {

    private Long id;
    private String title;
    private String description;
    private Long topicId;
    private String topicTitle;
    private Long courseId;
    private String courseTitle;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Boolean isRandomized;
    private Boolean isPublished;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime publishedAt;
    private Integer questionCount;
    private Integer totalPoints;
    private Integer submissionCount;
    private List<TestQuestionResponse> questions;
}
