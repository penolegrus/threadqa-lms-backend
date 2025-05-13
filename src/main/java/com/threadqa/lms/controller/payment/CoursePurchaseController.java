package com.threadqa.lms.controller.payment;

import com.threadqa.lms.dto.payment.CoursePurchaseRequest;
import com.threadqa.lms.dto.payment.CoursePurchaseResponse;
import com.threadqa.lms.service.payment.CoursePurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class CoursePurchaseController {

    private final CoursePurchaseService coursePurchaseService;

    /**
     * Эндпоинт для покупки курса
     *
     * @param request запрос на покупку курса
     * @param userDetails данные аутентифицированного пользователя
     * @return информация о покупке
     */
    @PostMapping("/course")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CoursePurchaseResponse> purchaseCourse(
            @Valid @RequestBody CoursePurchaseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        CoursePurchaseResponse response = coursePurchaseService.purchaseCourse(request, userId);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Эндпоинт для проверки доступности курса для покупки
     *
     * @param courseId ID курса
     * @param promoCode промокод (опционально)
     * @param userDetails данные аутентифицированного пользователя
     * @return информация о доступности курса и его цене
     */
    @GetMapping("/course/{courseId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CoursePurchaseResponse> checkCoursePurchase(
            @PathVariable Long courseId,
            @RequestParam(required = false) String promoCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        
        CoursePurchaseRequest request = new CoursePurchaseRequest();
        request.setCourseId(courseId);
        request.setPromoCode(promoCode);
        
        // Используем тот же сервис, но с флагом, что это только проверка
        CoursePurchaseResponse response = coursePurchaseService.purchaseCourse(request, userId);
        
        return ResponseEntity.ok(response);
    }
}
