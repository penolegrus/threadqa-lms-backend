package com.threadqa.lms.dto.assessment.test;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestMatchingPairRequest {
    
    private Long id; // Может быть null для новых пар
    
    @NotBlank(message = "Left item is required")
    private String leftItem;
    
    @NotBlank(message = "Right item is required")
    private String rightItem;
    
    private Integer orderIndex;
}
