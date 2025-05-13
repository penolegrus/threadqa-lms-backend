package com.threadqa.lms.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrResponse {
    private String qrcId;
    private String payload;
    private QrImage image;
    private Long iat;
}
