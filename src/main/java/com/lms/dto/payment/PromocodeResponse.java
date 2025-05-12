package com.lms.dto.payment;

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
public class PromocodeResponse {

    private Long id;
    private String code;
    private Integer discountPercentage;
    private BigDecimal discountAmount;
    private Boolean isPercentage;
    private Integer maxUses;
    private Integer currentUses;
    private ZonedDateTime validFrom;
    private ZonedDateTime validUntil;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}