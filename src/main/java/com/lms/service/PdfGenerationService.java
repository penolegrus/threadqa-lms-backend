package com.lms.service;

import com.lms.model.Certificate;
import org.springframework.stereotype.Service;

@Service
public class PdfGenerationService {

    public byte[] generateCertificatePdf(Certificate certificate) {
        // In a real implementation, you would use a PDF library like iText or PDFBox
        // to generate a certificate PDF

        // For now, we'll just return a placeholder byte array
        String content = "Certificate of Completion\n\n" +
                "This is to certify that\n\n" +
                certificate.getUser().getFirstName() + " " + certificate.getUser().getLastName() + "\n\n" +
                "has successfully completed the course\n\n" +
                certificate.getCourse().getTitle() + "\n\n" +
                "Certificate Number: " + certificate.getCertificateNumber() + "\n" +
                "Verification Code: " + certificate.getVerificationCode() + "\n" +
                "Issue Date: " + certificate.getIssueDate();

        return content.getBytes();
    }
}