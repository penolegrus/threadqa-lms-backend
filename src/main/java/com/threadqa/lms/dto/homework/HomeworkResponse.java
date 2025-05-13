package com.threadqa.lms.dto.homework;

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
public class HomeworkResponse {

    private Long id;
    private String title;
    private String description;
    private Long topicId;
    private String topicTitle;
    private Long courseId;
    private String courseTitle;
    private Integer maxScore;
    private ZonedDateTime dueDate;
    private Boolean isPublished;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime publishedAt;
    private Integer submissionCount;
    private Integer completedCount;
    private Double averageScore;
    private Boolean isSubmitted;
    private HomeworkSubmissionResponse userSubmission;
    private List<HomeworkRequirementResponse> requirements;
}
