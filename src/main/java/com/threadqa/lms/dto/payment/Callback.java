package com.threadqa.lms.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback {
    private String qrcId;
    private String transactionId;
    private String payerName;
    private Integer amount;
    private Long iat;
}
