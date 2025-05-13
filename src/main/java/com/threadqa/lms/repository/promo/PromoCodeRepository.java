package com.threadqa.lms.repository.promo;

import com.threadqa.lms.model.promo.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true AND p.validFrom <= :now AND (p.validTo IS NULL OR p.validTo >= :now)")
    List<PromoCode> findActivePromoCodesAt(ZonedDateTime now);

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true AND p.validFrom <= :now AND (p.validTo IS NULL OR p.validTo >= :now) AND (p.maxUses IS NULL OR p.currentUses < p.maxUses)")
    List<PromoCode> findValidPromoCodesAt(ZonedDateTime now);

    @Query("SELECT p FROM PromoCode p JOIN p.applicableCourses c WHERE c.id = :courseId AND p.isActive = true AND p.validFrom <= :now AND (p.validTo IS NULL OR p.validTo >= :now) AND (p.maxUses IS NULL OR p.currentUses < p.maxUses)")
    List<PromoCode> findValidPromoCodesForCourseAt(Long courseId, ZonedDateTime now);
}
