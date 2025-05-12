package com.lms.service;

import com.lms.dto.certificate.CertificateRequest;
import com.lms.dto.certificate.CertificateResponse;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.CertificateMapper;
import com.lms.model.Certificate;
import com.lms.model.Course;
import com.lms.model.User;
import com.lms.repository.CertificateRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CertificateMapper certificateMapper;
    private final PdfGenerationService pdfGenerationService;

    @Transactional
    public CertificateResponse createCertificate(CertificateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Check if certificate already exists
        certificateRepository.findByUserAndCourse(user, course)
                .ifPresent(certificate -> {
                    throw new IllegalStateException("Certificate already exists for this user and course");
                });

        String certificateNumber = generateCertificateNumber(user, course);
        String verificationCode = generateVerificationCode();

        Certificate certificate = Certificate.builder()
                .user(user)
                .course(course)
                .certificateNumber(certificateNumber)
                .verificationCode(verificationCode)
                .issueDate(ZonedDateTime.now())
                .expiryDate(request.getExpiryDate())
                .isRevoked(false)
                .build();

        Certificate savedCertificate = certificateRepository.save(certificate);

        // Generate PDF certificate
        String certificateUrl = generateCertificatePdf(savedCertificate);
        savedCertificate.setCertificateUrl(certificateUrl);

        savedCertificate = certificateRepository.save(savedCertificate);

        return certificateMapper.toCertificateResponse(savedCertificate);
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        return certificateMapper.toCertificateResponse(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateResponse verifyCertificate(String verificationCode) {
        Certificate certificate = certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        return certificateMapper.toCertificateResponse(certificate);
    }

    @Transactional(readOnly = true)
    public Page<CertificateResponse> getUserCertificates(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Certificate> certificates = certificateRepository.findByUser(user, pageable);

        List<CertificateResponse> certificateResponses = certificates.getContent().stream()
                .map(certificateMapper::toCertificateResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(certificateResponses, pageable, certificates.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<CertificateResponse> getCourseCertificates(Long courseId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Page<Certificate> certificates = certificateRepository.findByCourse(course, pageable);

        List<CertificateResponse> certificateResponses = certificates.getContent().stream()
                .map(certificateMapper::toCertificateResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(certificateResponses, pageable, certificates.getTotalElements());
    }

    @Transactional
    public CertificateResponse issueCertificate(CertificateRequest request) {
        return createCertificate(request);
    }

    @Transactional
    public CertificateResponse revokeCertificate(Long certificateId, String reason) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        if (certificate.getIsRevoked()) {
            throw new IllegalStateException("Certificate is already revoked");
        }

        certificate.setIsRevoked(true);
        certificate.setRevocationReason(reason);
        certificate.setRevocationDate(ZonedDateTime.now());

        Certificate updatedCertificate = certificateRepository.save(certificate);

        return certificateMapper.toCertificateResponse(updatedCertificate);
    }

    @Transactional(readOnly = true)
    public byte[] downloadCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        // In a real implementation, you would retrieve the PDF from storage
        // or generate it on-the-fly
        return pdfGenerationService.generateCertificatePdf(certificate);
    }

    private String generateCertificateNumber(User user, Course course) {
        // Format: COURSE-PREFIX-YEAR-USERID-RANDOM
        String coursePrefix = course.getTitle().substring(0, Math.min(3, course.getTitle().length())).toUpperCase();
        String year = String.valueOf(ZonedDateTime.now().getYear());
        String userId = String.format("%06d", user.getId());
        String random = String.format("%04d", (int) (Math.random() * 10000));

        return coursePrefix + "-" + year + "-" + userId + "-" + random;
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateCertificatePdf(Certificate certificate) {
        // In a real implementation, you would generate a PDF and store it
        // For now, we'll just return a placeholder URL
        return "/certificates/" + certificate.getId() + ".pdf";
    }
}