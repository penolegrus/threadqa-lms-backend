package com.lms.repository;

import com.lms.model.ReferralCode;
import com.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralCodeRepository extends JpaRepository<ReferralCode, Long> {

    Optional<ReferralCode> findByCodeAndIsActiveTrue(String code);

    List<ReferralCode> findByUserAndIsActiveTrue(User user);
}