package com.lms.dto.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Short description is required")
    private String shortDescription;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive or zero")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Level is required")
    private String level;

    private String imageUrl;
    private String coverImageUrl;

    @NotEmpty(message = "At least one instructor is required")
    private Set<Long> instructorIds;

    private Set<String> skills;
    private String duration;

    @NotBlank(message = "Status is required")
    private String status;
}