package com.threadqa.lms.dto.learningpath;

import com.threadqa.lms.model.learning.LearningPath;
import jakarta.validation.constraints.NotBlank;
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
public class LearningPathRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be less than 255 characters")
    private String slug;
    
    @NotNull(message = "Difficulty level is required")
    private LearningPath.DifficultyLevel difficultyLevel;
    
    private Integer estimatedHours;
    
    private boolean published;
    
    private boolean featured;
    
    private String thumbnailUrl;
    
    private String tags;
}
