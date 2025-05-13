package com.threadqa.lms.service.certificate;

import com.threadqa.lms.dto.certificate.CertificateRequest;
import com.threadqa.lms.dto.certificate.CertificateResponse;
import com.threadqa.lms.dto.certificate.CertificateVerificationRequest;
import com.threadqa.lms.dto.certificate.CertificateVerificationResponse;
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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CertificateMapper certificateMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public CertificateResponse generateCertificate(CertificateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        CourseEnrollment enrollment = courseEnrollmentRepository.findByUserIdAndCourseId(request.getUserId(), request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for user id: " + request.getUserId() + " and course id: " + request.getCourseId()));

        if (!enrollment.isCompleted()) {
            throw new IllegalStateException("Course is not completed yet");
        }

        // Generate certificate
        Certificate certificate = new Certificate();
        certificate.setUser(user);
        certificate.setCourse(course);
        certificate.setIssueDate(LocalDateTime.now());
        certificate.setVerificationCode(UUID.randomUUID().toString());

        // Generate PDF and save it
        String filePath = generateCertificatePdf(certificate);
        certificate.setFilePath(filePath);

        Certificate savedCertificate = certificateRepository.save(certificate);
        return certificateMapper.toCertificateResponse(savedCertificate);
    }

    private String generateCertificatePdf(Certificate certificate) {
        User user = certificate.getUser();
        Course course = certificate.getCourse();

        // Создаем поток для записи данных PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Здесь должен быть код для генерации PDF с использованием библиотеки, например iText
        // Пример:
        // Document document = new Document();
        // PdfWriter.getInstance(document, outputStream);
        // document.open();
        // document.add(new Paragraph("Certificate of Completion"));
        // document.add(new Paragraph("This is to certify that"));
        // document.add(new Paragraph(user.getFirstName() + " " + user.getLastName()));
        // document.add(new Paragraph("has successfully completed the course"));
        // document.add(new Paragraph(course.getTitle()));
        // document.add(new Paragraph("Date: " + certificate.getIssueDate()));
        // document.add(new Paragraph("Verification Code: " + certificate.getVerificationCode()));
        // document.close();

        // Для примера создаем простой текстовый "PDF"
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.println("Certificate of Completion");
            writer.println("This is to certify that");
            writer.println(user.getFirstName() + " " + user.getLastName());
            writer.println("has successfully completed the course");
            writer.println(course.getTitle());
            writer.println("Date: " + certificate.getIssueDate());
            writer.println("Verification Code: " + certificate.getVerificationCode());
        }

        // Создаем имя файла
        String fileName = "certificate_" + certificate.getVerificationCode() + ".pdf";

        // Сохраняем файл с использованием FileStorageService
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return fileStorageService.storeFile(inputStream, fileName);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getUserCertificates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Certificate> certificates = certificateRepository.findByUserId(userId);
        return certificates.stream()
                .map(certificateMapper::toCertificateResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));

        return certificateMapper.toCertificateResponse(certificate);
    }

    @Transactional(readOnly = true)
    public Resource downloadCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));

        return fileStorageService.loadFileAsResource(certificate.getFilePath());
    }

    @Transactional(readOnly = true)
    public CertificateVerificationResponse verifyCertificate(CertificateVerificationRequest request) {
        Certificate certificate = certificateRepository.findByVerificationCode(request.getVerificationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with verification code: " + request.getVerificationCode()));

        return CertificateVerificationResponse.builder()
                .isValid(true)
                .certificateId(certificate.getId())
                .userName(certificate.getUser().getFirstName() + " " + certificate.getUser().getLastName())
                .courseTitle(certificate.getCourse().getTitle())
                .issueDate(certificate.getIssueDate())
                .build();
    }
}
