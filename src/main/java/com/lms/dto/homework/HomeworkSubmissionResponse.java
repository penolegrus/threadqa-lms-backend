package com.lms.dto.homework;

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
public class HomeworkSubmissionResponse {

    private Long id;
    private Long homeworkId;
    private String homeworkTitle;
    private Long userId;
    private String userName;
    private String submissionText;
    private String githubUrl;
    private String additionalNotes;
    private List<String> fileUrls;
    private Integer score;
    private String feedback;
    private Boolean isGraded;
    private ZonedDateTime submittedAt;
    private ZonedDateTime gradedAt;
    private List<HomeworkChatMessageResponse> recentMessages;
}