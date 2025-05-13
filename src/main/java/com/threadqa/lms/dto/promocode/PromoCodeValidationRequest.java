package com.threadqa.lms.dto.promocode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeValidationRequest {

    @NotBlank(message = "Promo code is required")
    private String code;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}
