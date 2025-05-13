package com.threadqa.lms.repository.referral;

import com.threadqa.lms.model.referral.ReferralReward;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReferralRewardRepository extends JpaRepository<ReferralReward, Long> {
    
    List<ReferralReward> findByUser(User user);
    
    List<ReferralReward> findByUserAndIsProcessedFalse(User user);
    
    @Query("SELECT SUM(rr.rewardAmount) FROM ReferralReward rr WHERE rr.user.id = :userId AND rr.rewardType = 'CREDIT'")
    BigDecimal sumCreditRewardsByUserId(Long userId);
    
    @Query("SELECT COUNT(rr) FROM ReferralReward rr WHERE rr.user.id = :userId AND rr.rewardType = 'FREE_COURSE'")
    long countFreeCourseRewardsByUserId(Long userId);
}
