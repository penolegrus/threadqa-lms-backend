package com.lms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Integer orderIndex;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Integer contentCount;
}