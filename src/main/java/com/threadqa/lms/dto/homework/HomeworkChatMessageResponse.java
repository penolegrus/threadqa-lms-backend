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
public class HomeworkChatMessageResponse {

    private Long id;
    private Long submissionId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private List<String> fileUrls;
    private ZonedDateTime sentAt;
    private Boolean isRead;
}
