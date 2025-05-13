package com.threadqa.lms.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации о сессии пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoResponse {
    
    private Long id;
    private String ipAddress;
    private String city;
    private String country;
    private String deviceType;
    private String browser;
    private String operatingSystem;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private Boolean isCurrent;
}
