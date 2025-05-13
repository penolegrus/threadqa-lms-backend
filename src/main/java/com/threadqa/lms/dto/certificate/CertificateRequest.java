package com.threadqa.lms.dto.certificate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private LocalDateTime expiryDate;
}
