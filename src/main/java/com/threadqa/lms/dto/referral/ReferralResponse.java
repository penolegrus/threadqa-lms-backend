package com.threadqa.lms.dto.referral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralResponse {
    private Long id;
    private String email;
    private LocalDateTime invitedAt;
    private LocalDateTime registeredAt;
    private boolean isConverted;
    private String referralCode;
}
