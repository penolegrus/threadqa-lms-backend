package com.threadqa.lms.dto.referral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatisticsResponse {
    private Long userId;
    private String userName;
    private long totalInvitations;
    private long successfulReferrals;
    private BigDecimal totalCreditEarned;
    private long totalFreeCoursesEarned;
    private long invitationsThisMonth;
    private long conversionRate;
}
