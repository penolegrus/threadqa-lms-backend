package com.threadqa.lms.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePurchaseResponse {

    private Long paymentId;
    private Long courseId;
    private String courseTitle;
    private BigDecimal amount;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String invoiceNumber;
    private String promoCodeUsed;
    private Boolean accessGranted;
    private String paymentUrl; // URL для оплаты, если требуется перенаправление
    private QrResponse qrResponse; // Информация о QR-коде, если метод оплаты - QR
}
