package com.lms.controller;

import com.lms.dto.payment.*;
import com.lms.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PaymentResponse response = paymentService.initiatePayment(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> completePayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.completePayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PaymentResponse response = paymentService.getPayment(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<PaymentResponse> response = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promocodes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocodeResponse> createPromocode(@Valid @RequestBody PromocodeRequest request) {
        PromocodeResponse response = paymentService.createPromocode(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/promocodes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromocodeResponse>> getAllPromocodes() {
        List<PromocodeResponse> response = paymentService.getAllPromocodes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promocodes/active")
    public ResponseEntity<List<PromocodeResponse>> getActivePromocodes() {
        List<PromocodeResponse> response = paymentService.getActivePromocodes();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/referral-codes")
    public ResponseEntity<ReferralCodeResponse> createReferralCode(
            @Valid @RequestBody ReferralCodeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReferralCodeResponse response = paymentService.createReferralCode(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/referral-codes/user")
    public ResponseEntity<ReferralCodeResponse> getUserReferralCode(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReferralCodeResponse response = paymentService.getUserReferralCode(userId);
        return ResponseEntity.ok(response);
    }
}