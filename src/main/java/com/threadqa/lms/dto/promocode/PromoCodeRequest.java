package com.threadqa.lms.dto.promocode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Description is required")
    private String description;

    @Min(value = 0, message = "Discount percent must be at least 0")
    @Max(value = 100, message = "Discount percent must be at most 100")
    private Integer discountPercent;

    @Min(value = 0, message = "Discount amount must be at least 0")
    private Double discountAmount;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private Integer maxUses;

    @NotNull(message = "Valid from date is required")
    private ZonedDateTime validFrom;

    private ZonedDateTime validTo;

    private Set<Long> applicableCourseIds;
}
