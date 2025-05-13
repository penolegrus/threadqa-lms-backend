package com.threadqa.lms.service.referral;

import com.threadqa.lms.dto.referral.ReferralCodeResponse;
import com.threadqa.lms.dto.referral.ReferralRequest;
import com.threadqa.lms.dto.referral.ReferralResponse;
import com.threadqa.lms.dto.referral.ReferralStatisticsResponse;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.ReferralMapper;
import com.threadqa.lms.model.referral.ReferralCode;
import com.threadqa.lms.model.referral.ReferralInvitation;
import com.threadqa.lms.model.referral.ReferralReward;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.referral.ReferralCodeRepository;
import com.threadqa.lms.repository.referral.ReferralInvitationRepository;
import com.threadqa.lms.repository.referral.ReferralRewardRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.auth.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralCodeRepository referralCodeRepository;
    private final ReferralInvitationRepository referralInvitationRepository;
    private final ReferralRewardRepository referralRewardRepository;
    private final UserRepository userRepository;
    private final ReferralMapper referralMapper;
    private final EmailService emailService;
    
    @Value("${app.referral.code.length:8}")
    private int referralCodeLength;
    
    @Value("${app.referral.reward.amount:10.00}")
    private BigDecimal referralRewardAmount;
    
    @Transactional
    public ReferralCodeResponse generateReferralCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        String code;
        do {
            code = RandomStringUtils.randomAlphanumeric(referralCodeLength);
        } while (referralCodeRepository.existsByCode(code));
        
        ReferralCode referralCode = ReferralCode.builder()
                .code(code)
                .user(user)
                .isActive(true)
                .usageCount(0)
                .build();
        
        referralCode = referralCodeRepository.save(referralCode);
        return referralMapper.toReferralCodeResponse(referralCode);
    }
    
    @Transactional(readOnly = true)
    public List<ReferralCodeResponse> getUserReferralCodes(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<ReferralCode> referralCodes = referralCodeRepository.findActiveCodesByUserId(userId);
        return referralMapper.toReferralCodeResponseList(referralCodes);
    }
    
    @Transactional
    public List<ReferralResponse> sendReferralInvitations(Long userId, ReferralRequest referralRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        ReferralCode referralCode = referralCodeRepository.findByCode(referralRequest.getReferralCode())
                .orElseThrow(() -> new BadRequestException("Invalid referral code"));
        
        if (!referralCode.getUser().getId().equals(userId)) {
            throw new BadRequestException("This referral code does not belong to the current user");
        }
        
        if (!referralCode.isActive()) {
            throw new BadRequestException("This referral code is no longer active");
        }
        
        if (referralCode.getMaxUsage() != null && referralCode.getUsageCount() >= referralCode.getMaxUsage()) {
            throw new BadRequestException("This referral code has reached its maximum usage limit");
        }
        
        List<ReferralInvitation> invitations = referralRequest.getEmails().stream()
                .map(email -> {
                    ReferralInvitation invitation = ReferralInvitation.builder()
                            .referralCode(referralCode)
                            .email(email)
                            .isConverted(false)
                            .build();
                    
                    // Send email invitation
                    sendReferralEmail(user, email, referralCode.getCode(), referralRequest.getMessage());
                    
                    return referralInvitationRepository.save(invitation);
                })
                .toList();
        
        // Update usage count
        referralCode.setUsageCount(referralCode.getUsageCount() + invitations.size());
        referralCodeRepository.save(referralCode);
        
        return referralMapper.toReferralResponseList(invitations);
    }
    
    @Transactional(readOnly = true)
    public List<ReferralResponse> getUserReferrals(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<ReferralInvitation> invitations = referralInvitationRepository.findByUserId(userId);
        return referralMapper.toReferralResponseList(invitations);
    }
    
    @Transactional
    public void processReferralRegistration(String email, User registeredUser) {
        Optional<ReferralInvitation> invitationOpt = referralInvitationRepository.findByEmailAndIsConvertedFalse(email);
        
        if (invitationOpt.isPresent()) {
            ReferralInvitation invitation = invitationOpt.get();
            invitation.setRegisteredUser(registeredUser);
            invitation.setRegisteredAt(LocalDateTime.now());
            invitation.setConverted(true);
            referralInvitationRepository.save(invitation);
            
            // Create reward for the referrer
            User referrer = invitation.getReferralCode().getUser();
            ReferralReward reward = ReferralReward.builder()
                    .user(referrer)
                    .referralInvitation(invitation)
                    .rewardAmount(referralRewardAmount)
                    .rewardType(ReferralReward.RewardType.CREDIT)
                    .isProcessed(false)
                    .build();
            
            referralRewardRepository.save(reward);
        }
    }
    
    @Transactional(readOnly = true)
    public ReferralStatisticsResponse getUserReferralStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        long totalInvitations = referralInvitationRepository.findByUserId(userId).size();
        long successfulReferrals = referralInvitationRepository.countSuccessfulReferralsByUserId(userId);
        BigDecimal totalCreditEarned = referralRewardRepository.sumCreditRewardsByUserId(userId);
        long totalFreeCoursesEarned = referralRewardRepository.countFreeCourseRewardsByUserId(userId);
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long invitationsThisMonth = referralInvitationRepository.countReferralsSinceDate(userId, startOfMonth);
        
        return referralMapper.toReferralStatisticsResponse(
                userId,
                user.getUsername(),
                totalInvitations,
                successfulReferrals,
                totalCreditEarned != null ? totalCreditEarned : BigDecimal.ZERO,
                totalFreeCoursesEarned,
                invitationsThisMonth
        );
    }
    
    private void sendReferralEmail(User referrer, String recipientEmail, String referralCode, String customMessage) {
        String subject = referrer.getUsername() + " has invited you to join our learning platform";
        
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Hello,\n\n");
        messageBuilder.append(referrer.getUsername()).append(" has invited you to join our learning platform.\n\n");
        
        if (customMessage != null && !customMessage.isEmpty()) {
            messageBuilder.append("Personal message: ").append(customMessage).append("\n\n");
        }
        
        messageBuilder.append("Use this referral code when signing up to get special benefits: ").append(referralCode).append("\n\n");
        messageBuilder.append("Click here to sign up: [Registration Link]\n\n");
        messageBuilder.append("Thank you,\nThe Learning Platform Team");
        
        emailService.sendSimpleMessage(recipientEmail, subject, messageBuilder.toString());
    }
}
