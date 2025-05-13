package com.threadqa.lms.dto.assessment.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestMatchingPairResponse {
    
    private Long id;
    private Long questionId;
    private String leftItem;
    private String rightItem;
    private Integer orderIndex;
}
