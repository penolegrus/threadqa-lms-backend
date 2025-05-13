package com.threadqa.lms.dto.learningpath;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItemRequest {
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @Min(value = 0, message = "Position must be a positive number")
    private Integer position;
    
    private boolean required = true;
    
    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;
}
