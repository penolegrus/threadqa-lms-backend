package com.threadqa.lms.repository.referral;

import com.threadqa.lms.model.referral.ReferralCode;
import com.threadqa.lms.model.referral.ReferralInvitation;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralInvitationRepository extends JpaRepository<ReferralInvitation, Long> {
    
    List<ReferralInvitation> findByReferralCode(ReferralCode referralCode);
    
    List<ReferralInvitation> findByReferralCodeAndIsConvertedTrue(ReferralCode referralCode);
    
    Optional<ReferralInvitation> findByEmailAndIsConvertedFalse(String email);
    
    @Query("SELECT ri FROM ReferralInvitation ri WHERE ri.referralCode.user.id = :userId")
    List<ReferralInvitation> findByUserId(Long userId);
    
    @Query("SELECT COUNT(ri) FROM ReferralInvitation ri WHERE ri.referralCode.user.id = :userId AND ri.isConverted = true")
    long countSuccessfulReferralsByUserId(Long userId);
    
    @Query("SELECT COUNT(ri) FROM ReferralInvitation ri WHERE ri.referralCode.user.id = :userId AND ri.invitedAt >= :startDate")
    long countReferralsSinceDate(Long userId, LocalDateTime startDate);
}
