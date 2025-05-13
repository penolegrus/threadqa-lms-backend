package com.threadqa.lms.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrcIdDTO {

    private Long id;
    private String qrcId;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private ZonedDateTime createdAt;
    private ZonedDateTime expiresAt;
    private Boolean isUsed;
}
