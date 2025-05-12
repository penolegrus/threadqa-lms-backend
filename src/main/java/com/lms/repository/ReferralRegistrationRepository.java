package com.lms.repository;

import com.lms.model.ReferralCode;
import com.lms.model.ReferralRegistration;
import com.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRegistrationRepository extends JpaRepository<ReferralRegistration, Long> {

    List<ReferralRegistration> findByReferralCode(ReferralCode referralCode);

    List<ReferralRegistration> findByReferredUser(User referredUser);

    @Query("SELECT COUNT(rr) FROM ReferralRegistration rr WHERE rr.referralCode.user.id = :userId AND rr.isConverted = true")
    Long countSuccessfulReferrals(Long userId);
}