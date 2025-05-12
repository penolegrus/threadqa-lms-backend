package com.lms.dto.stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long instructorId;
    private String instructorName;
    private String streamKey;
    private String streamUrl;
    private ZonedDateTime scheduledStartTime;
    private ZonedDateTime actualStartTime;
    private ZonedDateTime endTime;
    private Boolean isLive;
    private Boolean isRecorded;
    private String recordingUrl;
    private Long activeViewers;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}