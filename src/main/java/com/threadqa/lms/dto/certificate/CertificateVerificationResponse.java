package com.threadqa.lms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateVerificationResponse {
    private boolean valid;
    private String certificateNumber;
    private String userName;
    private String courseName;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private boolean expired;
}
