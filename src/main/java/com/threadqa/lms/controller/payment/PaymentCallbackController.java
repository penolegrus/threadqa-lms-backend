package com.threadqa.lms.controller.payment;

import com.threadqa.lms.service.payment.PaymentCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/callback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Callback", description = "API для обработки callback-ов от платежной системы")
public class PaymentCallbackController {

    private final PaymentCallbackService paymentCallbackService;

    @PostMapping
    @Operation(summary = "Обработать callback от платежной системы", 
               description = "Принимает JWT токен с информацией о платеже и обрабатывает его")
    public ResponseEntity<Void> handleCallback(@RequestBody String jwtToken) {
        log.info("Received payment callback");
        paymentCallbackService.handleCallback(jwtToken);
        return ResponseEntity.ok().build();
    }
}
