package com.threadqa.lms.repository.payment;

import com.threadqa.lms.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments for a specific user
     */
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all payments for a specific course
     */
    Page<Payment> findByCourseId(Long courseId, Pageable pageable);

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payment by invoice number
     */
    Optional<Payment> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find all payments with a specific status
     */
    Page<Payment> findByStatus(String status, Pageable pageable);

    /**
     * Find all payments for a user with a specific status
     */
    Page<Payment> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    /**
     * Find all payments for a course with a specific status
     */
    Page<Payment> findByCourseIdAndStatus(Long courseId, String status, Pageable pageable);

    /**
     * Find all payments created between two dates
     */
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find all refunded payments
     */
    Page<Payment> findByRefundedTrue(Pageable pageable);

    /**
     * Find all payments with amount greater than specified value
     */
    Page<Payment> findByAmountGreaterThan(BigDecimal amount, Pageable pageable);

    /**
     * Find all payments with amount less than specified value
     */
    Page<Payment> findByAmountLessThan(BigDecimal amount, Pageable pageable);

    /**
     * Find all payments with a specific payment method
     */
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);

    /**
     * Find all payments with a specific promo code
     */
    Page<Payment> findByPromoCode(String promoCode, Pageable pageable);

    /**
     * Count payments by status
     */
    long countByStatus(String status);

    /**
     * Count payments by user
     */
    long countByUserId(Long userId);

    /**
     * Count payments by course
     */
    long countByCourseId(Long courseId);

    /**
     * Get total amount of payments for a specific course
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.course.id = :courseId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountByCourseId(@Param("courseId") Long courseId);

    /**
     * Get total amount of payments for a specific user
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    /**
     * Get total amount of payments for a specific period
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find latest payments for a user
     */
    List<Payment> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find latest payments for a course
     */
    List<Payment> findTop5ByCourseIdOrderByCreatedAtDesc(Long courseId);

    /**
     * Check if user has any successful payment for a course
     */
    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status);
}
