package com.threadqa.lms.dto.promocode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeValidationResponse {

    private Boolean isValid;
    private String code;
    private String description;
    private Integer discountPercent;
    private Double discountAmount;
    private Double originalPrice;
    private Double discountedPrice;
    private String message;
}
