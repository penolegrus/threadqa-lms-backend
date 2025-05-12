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
public class TestResponse {

    private Long id;
    private String title;
    private String description;
    private Long topicId;
    private String topicTitle;
    private Integer timeLimit;
    private Integer passingPercentage;
    private Boolean isRandomOrder;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<TestQuestionResponse> questions;
}