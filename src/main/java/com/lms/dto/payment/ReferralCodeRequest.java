package com.lms.dto.payment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCodeRequest {

    @Min(value = 1, message = "Discount percentage must be at least 1")
    @Max(value = 100, message = "Discount percentage must be at most 100")
    @NotNull(message = "Discount percentage is required")
    private Integer discountPercentage;

    @NotNull(message = "Referrer reward amount is required")
    @Positive(message = "Referrer reward amount must be positive")
    private BigDecimal referrerRewardAmount;
}