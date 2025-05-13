package com.threadqa.lms.controller.certificate;

import com.threadqa.lms.dto.certificate.CertificateRequest;
import com.threadqa.lms.dto.certificate.CertificateResponse;
import com.threadqa.lms.dto.certificate.CertificateVerificationRequest;
import com.threadqa.lms.dto.certificate.CertificateVerificationResponse;
import com.threadqa.lms.service.certificate.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificate", description = "Certificate API")
public class CertificateController {

    private final CertificateService certificateService;
    
    @PostMapping
    @Operation(summary = "Generate a certificate for a completed course")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CertificateResponse> generateCertificate(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CertificateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CertificateResponse response = certificateService.generateCertificate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all certificates for the current user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CertificateResponse>> getUserCertificates(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<CertificateResponse> certificates = certificateService.getUserCertificates(userId);
        return ResponseEntity.ok(certificates);
    }
    
    @GetMapping("/{certificateId}")
    @Operation(summary = "Get a specific certificate by ID")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CertificateResponse> getCertificate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long certificateId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CertificateResponse certificate = certificateService.getCertificate(userId, certificateId);
        return ResponseEntity.ok(certificate);
    }
    
    @GetMapping("/download/{certificateId}")
    @Operation(summary = "Download a certificate as PDF")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable Long certificateId) {
        Resource resource = certificateService.downloadCertificate(certificateId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\"")
                .body(resource);
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify a certificate by its number")
    public ResponseEntity<CertificateVerificationResponse> verifyCertificate(
            @Valid @RequestBody CertificateVerificationRequest request) {
        CertificateVerificationResponse response = certificateService.verifyCertificate(request.getCertificateNumber());
        return ResponseEntity.ok(response);
    }
}
