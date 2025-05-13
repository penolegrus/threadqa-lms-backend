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
     * Находит все платежи для конкретного пользователя
     */
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    /**
     * Находит все платежи для конкретного курса
     */
    Page<Payment> findByCourseId(Long courseId, Pageable pageable);

    /**
     * Находит платеж по ID транзакции
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Находит платеж по номеру счета
     */
    Optional<Payment> findByInvoiceNumber(String invoiceNumber);

    /**
     * Находит все платежи с определенным статусом
     */
    Page<Payment> findByStatus(String status, Pageable pageable);

    /**
     * Находит все платежи пользователя с определенным статусом
     */
    Page<Payment> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    /**
     * Находит все платежи за курс с определенным статусом
     */
    Page<Payment> findByCourseIdAndStatus(Long courseId, String status, Pageable pageable);

    /**
     * Находит все платежи, созданные в указанный период
     */
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Находит все возвращенные платежи
     */
    Page<Payment> findByRefundedTrue(Pageable pageable);

    /**
     * Находит все платежи с суммой больше указанной
     */
    Page<Payment> findByAmountGreaterThan(BigDecimal amount, Pageable pageable);

    /**
     * Находит все платежи с суммой меньше указанной
     */
    Page<Payment> findByAmountLessThan(BigDecimal amount, Pageable pageable);

    /**
     * Находит все платежи с определенным способом оплаты
     */
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);

    /**
     * Находит все платежи с определенным промокодом
     */
    Page<Payment> findByPromoCode(String promoCode, Pageable pageable);

    /**
     * Подсчитывает количество платежей по статусу
     */
    long countByStatus(String status);

    /**
     * Подсчитывает количество платежей пользователя
     */
    long countByUserId(Long userId);

    /**
     * Подсчитывает количество платежей за курс
     */
    long countByCourseId(Long courseId);

    /**
     * Подсчитывает количество платежей, созданных в указанный период
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Подсчитывает количество платежей с определенным статусом, созданных в указанный период
     */
    long countByStatusAndCreatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получает общую сумму платежей за конкретный курс
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.course.id = :courseId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountByCourseId(@Param("courseId") Long courseId);

    /**
     * Получает общую сумму платежей конкретного пользователя
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    /**
     * Получает общую сумму платежей за указанный период
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Находит последние платежи пользователя
     */
    List<Payment> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Находит последние платежи за курс
     */
    List<Payment> findTop5ByCourseIdOrderByCreatedAtDesc(Long courseId);

    /**
     * Проверяет, есть ли у пользователя успешный платеж за курс
     */
    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status);
}
