package com.lms.dto.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionResponse {

    private Long id;
    private String questionText;
    private String questionType;
    private Integer points;
    private Integer orderIndex;
    private List<TestOptionResponse> options;
}