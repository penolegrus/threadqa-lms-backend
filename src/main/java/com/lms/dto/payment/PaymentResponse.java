package com.lms.dto.payment;

import com.lms.model.PaymentMethod;
import com.lms.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseName;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String transactionId;
    private ZonedDateTime paymentDate;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String promocode;
    private String referralCode;
}