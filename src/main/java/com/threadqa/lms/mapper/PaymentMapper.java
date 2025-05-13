package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.payment.PaymentRequest;
import com.threadqa.lms.dto.payment.PaymentResponse;
import com.threadqa.lms.model.payment.Payment;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования между моделью Payment и DTO
 */
@Component
public class PaymentMapper {

    /**
     * Преобразует модель Payment в DTO PaymentResponse
     *
     * @param payment модель платежа
     * @return DTO с информацией о платеже
     */
    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .userName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName())
                .courseId(payment.getCourse().getId())
                .courseTitle(payment.getCourse().getTitle())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .refunded(payment.isRefunded())
                .refundDate(payment.getRefundDate())
                .refundReason(payment.getRefundReason())
                .refundTransactionId(payment.getRefundTransactionId())
                .invoiceNumber(payment.getInvoiceNumber())
                .paymentDescription(payment.getPaymentDescription())
                .discountAmount(payment.getDiscountAmount())
                .promoCode(payment.getPromoCode())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    /**
     * Преобразует DTO PaymentRequest в модель Payment
     *
     * @param request DTO с данными для создания платежа
     * @return модель платежа
     */
    public Payment toPayment(PaymentRequest request) {
        if (request == null) {
            return null;
        }

        return Payment.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status("PENDING")
                .currency("RUB")
                .promoCode(request.getPromoCode())
                .build();
    }
}
