package com.lms.repository;

import com.lms.model.Payment;
import com.lms.model.PaymentStatus;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByUser(User user, Pageable pageable);

    List<Payment> findByUserAndStatus(User user, PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueForPeriod(ZonedDateTime startDate, ZonedDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.course.id = :courseId AND p.status = 'COMPLETED'")
    Long countCompletedPaymentsForCourse(Long courseId);
}