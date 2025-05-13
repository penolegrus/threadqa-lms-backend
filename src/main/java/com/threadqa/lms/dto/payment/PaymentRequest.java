package com.threadqa.lms.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String transactionId;

    private String invoiceNumber;

    private String paymentDescription;

    private String billingAddress;

    private String billingCity;

    private String billingCountry;

    private String billingPostalCode;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private String promoCode;
}
