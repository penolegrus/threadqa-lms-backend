package com.lms.dto.payment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocodeRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @Min(value = 1, message = "Discount percentage must be at least 1")
    @Max(value = 100, message = "Discount percentage must be at most 100")
    private Integer discountPercentage;

    private BigDecimal discountAmount;

    @NotNull(message = "Is percentage flag is required")
    private Boolean isPercentage;

    private Integer maxUses;

    @NotNull(message = "Valid from date is required")
    private ZonedDateTime validFrom;

    private ZonedDateTime validUntil;

    @NotNull(message = "Is active flag is required")
    private Boolean isActive;
}