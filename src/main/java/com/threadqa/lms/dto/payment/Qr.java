package com.threadqa.lms.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Qr {
    private String name;
    private String email;
    private Integer amount;
    private String paymentPurpose;
    private Integer ttl;
    private String redirectUrl;
    private String paymentCallbackUrl;
}
