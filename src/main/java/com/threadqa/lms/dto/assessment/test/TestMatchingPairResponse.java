package com.threadqa.lms.dto.assessment.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления пары соответствия в тестовом вопросе.
 * Используется для вопросов типа "сопоставление", где пользователь должен
 * сопоставить элементы из левой колонки с элементами из правой колонки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestMatchingPairResponse {
    
    /**
     * Уникальный идентификатор пары соответствия
     */
    private Long id;
    
    /**
     * Идентификатор вопроса, к которому относится пара
     */
    private Long questionId;
    
    /**
     * Элемент из левой колонки
     */
    private String leftItem;
    
    /**
     * Элемент из правой колонки, который соответствует leftItem
     */
    private String rightItem;
    
    /**
     * Порядковый индекс для отображения пары в списке
     */
    private Integer orderIndex;
}
