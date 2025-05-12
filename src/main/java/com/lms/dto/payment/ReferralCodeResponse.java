package com.lms.dto.payment;

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
public class ReferralCodeResponse {

    private Long id;
    private String code;
    private Long userId;
    private String userName;
    private Integer discountPercentage;
    private BigDecimal referrerRewardAmount;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Long successfulReferrals;
}