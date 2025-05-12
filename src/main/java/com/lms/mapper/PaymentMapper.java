package com.lms.mapper;

import com.lms.dto.payment.PaymentResponse;
import com.lms.dto.payment.PromocodeResponse;
import com.lms.dto.payment.ReferralCodeResponse;
import com.lms.model.Payment;
import com.lms.model.Promocode;
import com.lms.model.ReferralCode;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .userName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName())
                .courseId(payment.getCourse() != null ? payment.getCourse().getId() : null)
                .courseName(payment.getCourse() != null ? payment.getCourse().getTitle() : null)
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .promocode(payment.getPromocode() != null ? payment.getPromocode().getCode() : null)
                .referralCode(payment.getReferralCode() != null ? payment.getReferralCode().getCode() : null)
                .build();
    }

    public PromocodeResponse toPromocodeResponse(Promocode promocode) {
        if (promocode == null) {
            return null;
        }

        return PromocodeResponse.builder()
                .id(promocode.getId())
                .code(promocode.getCode())
                .discountPercentage(promocode.getDiscountPercentage())
                .discountAmount(promocode.getDiscountAmount())
                .isPercentage(promocode.getIsPercentage())
                .maxUses(promocode.getMaxUses())
                .currentUses(promocode.getCurrentUses())
                .validFrom(promocode.getValidFrom())
                .validUntil(promocode.getValidUntil())
                .isActive(promocode.getIsActive())
                .createdAt(promocode.getCreatedAt())
                .updatedAt(promocode.getUpdatedAt())
                .build();
    }

    public ReferralCodeResponse toReferralCodeResponse(ReferralCode referralCode, Long successfulReferrals) {
        if (referralCode == null) {
            return null;
        }

        return ReferralCodeResponse.builder()
                .id(referralCode.getId())
                .code(referralCode.getCode())
                .userId(referralCode.getUser().getId())
                .userName(referralCode.getUser().getFirstName() + " " + referralCode.getUser().getLastName())
                .discountPercentage(referralCode.getDiscountPercentage())
                .referrerRewardAmount(referralCode.getReferrerRewardAmount())
                .isActive(referralCode.getIsActive())
                .createdAt(referralCode.getCreatedAt())
                .updatedAt(referralCode.getUpdatedAt())
                .successfulReferrals(successfulReferrals)
                .build();
    }
}