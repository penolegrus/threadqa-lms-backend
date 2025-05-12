package com.lms.mapper;

import com.lms.dto.certificate.CertificateResponse;
import com.lms.model.Certificate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class CertificateMapper {

    public CertificateResponse toCertificateResponse(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        boolean isExpired = false;
        if (certificate.getExpiryDate() != null) {
            isExpired = ZonedDateTime.now().isAfter(certificate.getExpiryDate());
        }

        return CertificateResponse.builder()
                .id(certificate.getId())
                .userId(certificate.getUser().getId())
                .userName(certificate.getUser().getFirstName() + " " + certificate.getUser().getLastName())
                .courseId(certificate.getCourse().getId())
                .courseName(certificate.getCourse().getTitle())
                .certificateNumber(certificate.getCertificateNumber())
                .verificationCode(certificate.getVerificationCode())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .isRevoked(certificate.getIsRevoked())
                .revocationReason(certificate.getRevocationReason())
                .revocationDate(certificate.getRevocationDate())
                .certificateUrl(certificate.getCertificateUrl())
                .isExpired(isExpired)
                .build();
    }
}