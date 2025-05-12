package com.lms.dto.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestOptionResponse {

    private Long id;
    private String optionText;
    private Integer orderIndex;
    private Boolean isCorrect;
    private String feedback;
}