package com.threadqa.lms.controller.promo;

import com.threadqa.lms.dto.promocode.PromoCodeRequest;
import com.threadqa.lms.dto.promocode.PromoCodeResponse;
import com.threadqa.lms.dto.promocode.PromoCodeValidationRequest;
import com.threadqa.lms.dto.promocode.PromoCodeValidationResponse;
import com.threadqa.lms.service.promo.PromoCodeService;
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
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromoCodeResponse>> getAllPromoCodes() {
        List<PromoCodeResponse> promoCodes = promoCodeService.getAllPromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/{promoCodeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoCodeResponse> getPromoCode(@PathVariable Long promoCodeId) {
        PromoCodeResponse promoCode = promoCodeService.getPromoCode(promoCodeId);
        return ResponseEntity.ok(promoCode);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoCodeResponse> createPromoCode(@Valid @RequestBody PromoCodeRequest request) {
        PromoCodeResponse promoCode = promoCodeService.createPromoCode(request);
        return new ResponseEntity<>(promoCode, HttpStatus.CREATED);
    }

    @PutMapping("/{promoCodeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoCodeResponse> updatePromoCode(
            @PathVariable Long promoCodeId,
            @Valid @RequestBody PromoCodeRequest request) {
        PromoCodeResponse promoCode = promoCodeService.updatePromoCode(promoCodeId, request);
        return ResponseEntity.ok(promoCode);
    }

    @DeleteMapping("/{promoCodeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long promoCodeId) {
        promoCodeService.deletePromoCode(promoCodeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromoCodeValidationResponse> validatePromoCode(
            @Valid @RequestBody PromoCodeValidationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PromoCodeValidationResponse response = promoCodeService.validatePromoCode(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/use")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> usePromoCode(
            @RequestParam String code,
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        promoCodeService.usePromoCode(code, courseId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}/valid")
    public ResponseEntity<List<PromoCodeResponse>> getValidPromoCodesForCourse(@PathVariable Long courseId) {
        List<PromoCodeResponse> promoCodes = promoCodeService.getValidPromoCodesForCourse(courseId);
        return ResponseEntity.ok(promoCodes);
    }
}
