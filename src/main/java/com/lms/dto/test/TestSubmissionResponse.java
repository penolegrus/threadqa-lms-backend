package com.lms.dto.test;

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
public class TestSubmissionResponse {

    private Long id;
    private Long testId;
    private String testTitle;
    private Long userId;
    private String userName;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private Boolean isPassed;
    private ZonedDateTime startedAt;
    private ZonedDateTime submittedAt;
    private Long timeSpentSeconds;
    private List<TestQuestionAnswerResponse> answers;
}