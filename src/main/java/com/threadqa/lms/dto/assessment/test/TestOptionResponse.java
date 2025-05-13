package com.threadqa.lms.dto.assessment;

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
    private Long questionId;
    private String content;
    private Boolean isCorrect;
    private Integer orderIndex;
    private String explanation;
}