package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.certificate.CertificateResponse;
import com.threadqa.lms.dto.certificate.CertificateVerificationResponse;
import com.threadqa.lms.model.certificate.Certificate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CertificateMapper {

    public CertificateResponse toCertificateResponse(Certificate certificate, String downloadUrl) {
        if (certificate == null) {
            return null;
        }
        
        return CertificateResponse.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .userId(certificate.getUser().getId())
                .userName(certificate.getUser().getUsername())
                .courseId(certificate.getCourse().getId())
                .courseName(certificate.getCourse().getTitle())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .filePath(certificate.getFilePath())
                .verificationUrl(certificate.getVerificationUrl())
                .downloadUrl(downloadUrl)
                .build();
    }
    
    public List<CertificateResponse> toCertificateResponseList(List<Certificate> certificates, String baseDownloadUrl) {
        if (certificates == null) {
            return null;
        }
        
        return certificates.stream()
                .map(certificate -> {
                    String downloadUrl = baseDownloadUrl + "/" + certificate.getId();
                    return toCertificateResponse(certificate, downloadUrl);
                })
                .collect(Collectors.toList());
    }
    
    public CertificateVerificationResponse toCertificateVerificationResponse(Certificate certificate) {
        if (certificate == null) {
            return CertificateVerificationResponse.builder()
                    .valid(false)
                    .build();
        }
        
        boolean expired = certificate.getExpiryDate() != null && 
                certificate.getExpiryDate().isBefore(LocalDateTime.now());
        
        return CertificateVerificationResponse.builder()
                .valid(true)
                .certificateNumber(certificate.getCertificateNumber())
                .userName(certificate.getUser().getUsername())
                .courseName(certificate.getCourse().getTitle())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .expired(expired)
                .build();
    }
}
