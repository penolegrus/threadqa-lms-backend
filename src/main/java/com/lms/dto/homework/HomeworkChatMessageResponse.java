package com.lms.dto.homework;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkChatMessageResponse {

    private Long id;
    private Long submissionId;
    private Long userId;
    private String userName;
    private String userRole;
    private String message;
    private ZonedDateTime sentAt;
}