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
public class CertificateResponse {
    private Long id;
    private String certificateNumber;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseName;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private String filePath;
    private String verificationUrl;
    private String downloadUrl;
}
