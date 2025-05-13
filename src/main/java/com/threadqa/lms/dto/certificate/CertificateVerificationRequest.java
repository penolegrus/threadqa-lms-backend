package com.threadqa.lms.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateVerificationRequest {
    
    @NotBlank(message = "Certificate number is required")
    private String certificateNumber;
}
