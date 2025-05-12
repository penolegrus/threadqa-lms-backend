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
public class StreamChatMessageResponse {

    private Long id;
    private Long streamId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String message;
    private ZonedDateTime sentAt;
    private Boolean isPinned;
    private Boolean isInstructorMessage;
}