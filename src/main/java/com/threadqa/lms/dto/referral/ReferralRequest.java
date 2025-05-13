package com.threadqa.lms.dto.referral;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralRequest {
    @NotBlank(message = "Referral code is required")
    private String referralCode;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private List<String> emails;
    
    private String message;
}
