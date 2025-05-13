package com.threadqa.lms.repository.promo;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.promo.PromoCode;
import com.threadqa.lms.model.promo.PromoCodeUsage;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {

    List<PromoCodeUsage> findByUser(User user);

    List<PromoCodeUsage> findByPromoCode(PromoCode promoCode);

    Optional<PromoCodeUsage> findByPromoCodeAndUserAndCourse(PromoCode promoCode, User user, Course course);

    @Query("SELECT COUNT(pu) FROM PromoCodeUsage pu WHERE pu.promoCode.id = :promoCodeId")
    Long countByPromoCodeId(Long promoCodeId);

    @Query("SELECT SUM(pu.discountAmount) FROM PromoCodeUsage pu WHERE pu.promoCode.id = :promoCodeId")
    Double getTotalDiscountByPromoCodeId(Long promoCodeId);
}
