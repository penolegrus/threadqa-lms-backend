package com.lms.controller;

import com.lms.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> createCertificate(@Valid @RequestBody CertificateRequest request) {
        CertificateResponse certificate = certificateService.createCertificate(request);
        return new ResponseEntity<>(certificate, HttpStatus.CREATED);
    }

    @GetMapping("/{certificateId}")
    public ResponseEntity<CertificateResponse> getCertificate(@PathVariable Long certificateId) {
        CertificateResponse certificate = certificateService.getCertificate(certificateId);
        return ResponseEntity.ok(certificate);
    }

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<CertificateResponse> verifyCertificate(@PathVariable String verificationCode) {
        CertificateResponse certificate = certificateService.verifyCertificate(verificationCode);
        return ResponseEntity.ok(certificate);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<CertificateResponse>> getUserCertificates(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<CertificateResponse> certificates = certificateService.getUserCertificates(userId, pageable);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<CertificateResponse>> getCourseCertificates(
            @PathVariable Long courseId,
            Pageable pageable) {
        Page<CertificateResponse> certificates = certificateService.getCourseCertificates(courseId, pageable);
        return ResponseEntity.ok(certificates);
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CertificateResponse> issueCertificate(
            @Valid @RequestBody CertificateRequest request) {
        CertificateResponse certificate = certificateService.issueCertificate(request);
        return new ResponseEntity<>(certificate, HttpStatus.CREATED);
    }

    @PostMapping("/{certificateId}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> revokeCertificate(
            @PathVariable Long certificateId,
            @RequestParam String reason) {
        CertificateResponse certificate = certificateService.revokeCertificate(certificateId, reason);
        return ResponseEntity.ok(certificate);
    }

    @GetMapping("/download/{certificateId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long certificateId) {
        byte[] certificateFile = certificateService.downloadCertificate(certificateId);
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=certificate.pdf")
                .body(certificateFile);
    }
}