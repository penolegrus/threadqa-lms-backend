package com.threadqa.lms.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи информации о платеже
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * Уникальный идентификатор платежа
     */
    private Long id;

    /**
     * Идентификатор пользователя, совершившего платеж
     */
    private Long userId;

    /**
     * Имя пользователя, совершившего платеж
     */
    private String userName;

    /**
     * Идентификатор курса, за который произведен платеж
     */
    private Long courseId;

    /**
     * Название курса, за который произведен платеж
     */
    private String courseTitle;

    /**
     * Сумма платежа
     */
    private BigDecimal amount;

    /**
     * Валюта платежа
     */
    private String currency;

    /**
     * Статус платежа (PENDING, COMPLETED, FAILED, REFUNDED)
     */
    private String status;

    /**
     * Способ оплаты (CARD, QR_CODE, BANK_TRANSFER, CRYPTO)
     */
    private String paymentMethod;

    /**
     * Идентификатор транзакции в платежной системе
     */
    private String transactionId;

    /**
     * Дата и время платежа
     */
    private LocalDateTime paymentDate;

    /**
     * Флаг, указывающий, был ли произведен возврат платежа
     */
    private boolean refunded;

    /**
     * Дата и время возврата платежа
     */
    private LocalDateTime refundDate;

    /**
     * Причина возврата платежа
     */
    private String refundReason;

    /**
     * Идентификатор транзакции возврата в платежной системе
     */
    private String refundTransactionId;

    /**
     * Номер счета/инвойса
     */
    private String invoiceNumber;

    /**
     * Описание платежа
     */
    private String paymentDescription;

    /**
     * Сумма скидки
     */
    private BigDecimal discountAmount;

    /**
     * Использованный промокод
     */
    private String promoCode;

    /**
     * Дата и время создания записи о платеже
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи о платеже
     */
    private LocalDateTime updatedAt;
}
