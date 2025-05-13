package com.threadqa.lms.repository.referral;

import com.threadqa.lms.model.referral.ReferralCode;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralCodeRepository extends JpaRepository<ReferralCode, Long> {
    
    Optional<ReferralCode> findByCode(String code);
    
    List<ReferralCode> findByUserAndIsActiveTrue(User user);
    
    @Query("SELECT rc FROM ReferralCode rc WHERE rc.user.id = :userId AND rc.isActive = true")
    List<ReferralCode> findActiveCodesByUserId(Long userId);
    
    boolean existsByCode(String code);
}
