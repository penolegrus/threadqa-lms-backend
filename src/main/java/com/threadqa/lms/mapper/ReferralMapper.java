package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.referral.ReferralCodeResponse;
import com.threadqa.lms.dto.referral.ReferralResponse;
import com.threadqa.lms.dto.referral.ReferralStatisticsResponse;
import com.threadqa.lms.model.referral.ReferralCode;
import com.threadqa.lms.model.referral.ReferralInvitation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReferralMapper {

    public ReferralCodeResponse toReferralCodeResponse(ReferralCode referralCode) {
        if (referralCode == null) {
            return null;
        }
        
        return ReferralCodeResponse.builder()
                .id(referralCode.getId())
                .code(referralCode.getCode())
                .userId(referralCode.getUser().getId())
                .userName(referralCode.getUser().getUsername())
                .createdAt(referralCode.getCreatedAt())
                .expiresAt(referralCode.getExpiresAt())
                .isActive(referralCode.isActive())
                .usageCount(referralCode.getUsageCount())
                .maxUsage(referralCode.getMaxUsage())
                .build();
    }
    
    public List<ReferralCodeResponse> toReferralCodeResponseList(List<ReferralCode> referralCodes) {
        if (referralCodes == null) {
            return null;
        }
        
        return referralCodes.stream()
                .map(this::toReferralCodeResponse)
                .collect(Collectors.toList());
    }
    
    public ReferralResponse toReferralResponse(ReferralInvitation invitation) {
        if (invitation == null) {
            return null;
        }
        
        return ReferralResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .invitedAt(invitation.getInvitedAt())
                .registeredAt(invitation.getRegisteredAt())
                .isConverted(invitation.isConverted())
                .referralCode(invitation.getReferralCode().getCode())
                .build();
    }
    
    public List<ReferralResponse> toReferralResponseList(List<ReferralInvitation> invitations) {
        if (invitations == null) {
            return null;
        }
        
        return invitations.stream()
                .map(this::toReferralResponse)
                .collect(Collectors.toList());
    }
    
    public ReferralStatisticsResponse toReferralStatisticsResponse(
            Long userId, 
            String userName,
            long totalInvitations, 
            long successfulReferrals, 
            BigDecimal totalCreditEarned,
            long totalFreeCoursesEarned,
            long invitationsThisMonth) {
        
        long conversionRate = totalInvitations > 0 
                ? (successfulReferrals * 100) / totalInvitations 
                : 0;
        
        return ReferralStatisticsResponse.builder()
                .userId(userId)
                .userName(userName)
                .totalInvitations(totalInvitations)
                .successfulReferrals(successfulReferrals)
                .totalCreditEarned(totalCreditEarned)
                .totalFreeCoursesEarned(totalFreeCoursesEarned)
                .invitationsThisMonth(invitationsThisMonth)
                .conversionRate(conversionRate)
                .build();
    }
}
