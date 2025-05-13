package com.threadqa.lms.dto.assessment.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO для представления информации о попытке прохождения теста пользователем.
 * Используется для передачи данных о результатах тестирования в пользовательский интерфейс.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionResponse {
    /**
     * Уникальный идентификатор попытки прохождения теста
     */
    private Long id;

    /**
     * Идентификатор теста, который проходил пользователь
     */
    private Long testId;

    /**
     * Название теста
     */
    private String testTitle;

    /**
     * Идентификатор пользователя, который проходил тест
     */
    private Long userId;

    /**
     * Имя пользователя (Имя Фамилия)
     */
    private String userName;

    /**
     * Дата и время начала прохождения теста
     */
    private ZonedDateTime startedAt;

    /**
     * Дата и время завершения прохождения теста
     */
    private ZonedDateTime submittedAt;

    /**
     * Набранный балл (процент правильных ответов)
     */
    private Integer score;

    /**
     * Флаг, указывающий, пройден ли тест успешно
     */
    private Boolean isPassed;

    /**
     * Номер попытки прохождения теста пользователем
     */
    private Integer attemptNumber;

    /**
     * Список ответов пользователя на вопросы теста
     */
    private List<TestAnswerResponse> answers;
}
