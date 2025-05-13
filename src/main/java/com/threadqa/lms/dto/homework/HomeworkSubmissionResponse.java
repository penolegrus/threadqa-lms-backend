package com.threadqa.lms.dto.homework;

import com.threadqa.lms.model.homework.HomeworkSubmission;
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
    private String content;
    private List<String> fileUrls;
    private ZonedDateTime submittedAt;
    private ZonedDateTime reviewedAt;
    private Long reviewerId;
    private String reviewerName;
    private Integer score;
    private String feedback;
    private HomeworkSubmission.SubmissionStatus status;
    private Integer unreadMessageCount;
}
