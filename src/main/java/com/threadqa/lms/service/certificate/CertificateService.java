package com.threadqa.lms.service.certificate;

import com.threadqa.lms.dto.certificate.CertificateRequest;
import com.threadqa.lms.dto.certificate.CertificateResponse;
import com.threadqa.lms.dto.certificate.CertificateVerificationResponse;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.CertificateMapper;
import com.threadqa.lms.model.certificate.Certificate;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.certificate.CertificateRepository;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CertificateMapper certificateMapper;
    private final FileStorageService fileStorageService;
    
    @Value("${app.certificate.verification.url:https://example.com/verify}")
    private String verificationBaseUrl;
    
    @Value("${app.certificate.download.url:https://example.com/certificates}")
    private String downloadBaseUrl;
    
    @Transactional
    public CertificateResponse generateCertificate(Long userId, CertificateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
        
        // Check if user has completed the course
        Optional<CourseEnrollment> enrollmentOpt = courseEnrollmentRepository.findByUserAndCourse(user, course);
        if (enrollmentOpt.isEmpty() || !enrollmentOpt.get().isCompleted()) {
            throw new BadRequestException("User has not completed this course yet");
        }
        
        // Check if certificate already exists
        if (certificateRepository.existsByUserAndCourse(user, course)) {
            throw new BadRequestException("Certificate already exists for this user and course");
        }
        
        // Generate certificate
        Certificate certificate = Certificate.builder()
                .user(user)
                .course(course)
                .expiryDate(request.getExpiryDate())
                .verificationUrl(verificationBaseUrl + "?code=")
                .build();
        
        certificate = certificateRepository.save(certificate);
        
        // Update verification URL with the certificate number
        certificate.setVerificationUrl(verificationBaseUrl + "?code=" + certificate.getCertificateNumber());
        
        // Generate certificate PDF
        String filePath = generateCertificatePdf(certificate);
        certificate.setFilePath(filePath);
        
        certificate = certificateRepository.save(certificate);
        
        String downloadUrl = downloadBaseUrl + "/" + certificate.getId();
        return certificateMapper.toCertificateResponse(certificate, downloadUrl);
    }
    
    @Transactional(readOnly = true)
    public List<CertificateResponse> getUserCertificates(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Certificate> certificates = certificateRepository.findByUserId(userId);
        return certificateMapper.toCertificateResponseList(certificates, downloadBaseUrl);
    }
    
    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(Long userId, Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));
        
        if (!certificate.getUser().getId().equals(userId)) {
            throw new BadRequestException("This certificate does not belong to the current user");
        }
        
        String downloadUrl = downloadBaseUrl + "/" + certificate.getId();
        return certificateMapper.toCertificateResponse(certificate, downloadUrl);
    }
    
    @Transactional(readOnly = true)
    public Resource downloadCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));
        
        try {
            return fileStorageService.loadFileAsResource(certificate.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Error loading certificate file", e);
        }
    }
    
    @Transactional(readOnly = true)
    public CertificateVerificationResponse verifyCertificate(String certificateNumber) {
        Optional<Certificate> certificateOpt = certificateRepository.findByCertificateNumber(certificateNumber);
        return certificateMapper.toCertificateVerificationResponse(certificateOpt.orElse(null));
    }
    
    private String generateCertificatePdf(Certificate certificate) {
        // This is a placeholder for actual PDF generation logic
        // In a real implementation, you would use a library like iText, PDFBox, or JasperReports
        
        String fileName = "certificate_" + certificate.getId() + ".pdf";
        String content = "Certificate of Completion\n\n" +
                "This is to certify that\n\n" +
                certificate.getUser().getUsername() + "\n\n" +
                "has successfully completed the course\n\n" +
                certificate.getCourse().getTitle() + "\n\n" +
                "Certificate Number: " + certificate.getCertificateNumber() + "\n" +
                "Issue Date: " + certificate.getIssueDate();
        
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
            return fileStorageService.storeFile(inputStream, fileName, "application/pdf");
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }
}
