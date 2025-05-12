package com.lms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseName;
    private String certificateNumber;
    private String verificationCode;
    private ZonedDateTime issueDate;
    private ZonedDateTime expiryDate;
    private Boolean isRevoked;
    private String revocationReason;
    private ZonedDateTime revocationDate;
    private String certificateUrl;
    private Boolean isExpired;
}