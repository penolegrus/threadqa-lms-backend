package com.threadqa.lms.dto.stream;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.threadqa.lms.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamParticipantResponse {
    
    private Long id;
    private Long streamId;
    private UserDTO user;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leftAt;
    
    private boolean isActive;
}
