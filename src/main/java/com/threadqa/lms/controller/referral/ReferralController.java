package com.threadqa.lms.controller.referral;

import com.threadqa.lms.dto.referral.ReferralCodeResponse;
import com.threadqa.lms.dto.referral.ReferralRequest;
import com.threadqa.lms.dto.referral.ReferralResponse;
import com.threadqa.lms.dto.referral.ReferralStatisticsResponse;
import com.threadqa.lms.service.referral.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
@Tag(name = "Referral", description = "Referral API")
public class ReferralController {

    private final ReferralService referralService;
    
    @PostMapping("/codes")
    @Operation(summary = "Generate a new referral code for the current user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferralCodeResponse> generateReferralCode(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReferralCodeResponse response = referralService.generateReferralCode(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/codes")
    @Operation(summary = "Get all active referral codes for the current user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ReferralCodeResponse>> getUserReferralCodes(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<ReferralCodeResponse> codes = referralService.getUserReferralCodes(userId);
        return ResponseEntity.ok(codes);
    }
    
    @PostMapping("/invite")
    @Operation(summary = "Send referral invitations to multiple email addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ReferralResponse>> sendReferralInvitations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReferralRequest referralRequest) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<ReferralResponse> invitations = referralService.sendReferralInvitations(userId, referralRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitations);
    }
    
    @GetMapping
    @Operation(summary = "Get all referrals sent by the current user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ReferralResponse>> getUserReferrals(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<ReferralResponse> referrals = referralService.getUserReferrals(userId);
        return ResponseEntity.ok(referrals);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get referral statistics for the current user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferralStatisticsResponse> getUserReferralStatistics(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReferralStatisticsResponse statistics = referralService.getUserReferralStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
