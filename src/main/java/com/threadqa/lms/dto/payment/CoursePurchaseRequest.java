package com.threadqa.lms.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePurchaseRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private String paymentMethod;
    
    private String promoCode;
    
    // Дополнительные поля для платежной информации, если необходимо
    private String billingAddress;
    private String billingCity;
    private String billingCountry;
    private String billingPostalCode;
}
