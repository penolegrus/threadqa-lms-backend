package com.threadqa.lms.controller.payment;

import com.threadqa.lms.dto.payment.QrcIdDTO;
import com.threadqa.lms.dto.payment.QrcIdRequest;
import com.threadqa.lms.service.payment.QrcIdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qrc-ids")
@RequiredArgsConstructor
public class QrcIdController {

    private final QrcIdService qrcIdService;

    @PostMapping
    public ResponseEntity<QrcIdDTO> generateQrcId(
            @Valid @RequestBody QrcIdRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        QrcIdDTO qrcIdDTO = qrcIdService.generateQrcId(request, userId);
        return new ResponseEntity<>(qrcIdDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{qrcId}")
    public ResponseEntity<QrcIdDTO> getQrcIdByCode(@PathVariable String qrcId) {
        QrcIdDTO qrcIdDTO = qrcIdService.getQrcIdByCode(qrcId);
        return ResponseEntity.ok(qrcIdDTO);
    }

    @GetMapping("/user")
    public ResponseEntity<List<QrcIdDTO>> getQrcIdsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<QrcIdDTO> qrcIdDTOs = qrcIdService.getQrcIdsByUser(userId);
        return ResponseEntity.ok(qrcIdDTOs);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QrcIdDTO>> getQrcIdsByCourse(@PathVariable Long courseId) {
        List<QrcIdDTO> qrcIdDTOs = qrcIdService.getQrcIdsByCourse(courseId);
        return ResponseEntity.ok(qrcIdDTOs);
    }

    @PostMapping("/{qrcId}/use")
    public ResponseEntity<QrcIdDTO> markQrcIdAsUsed(@PathVariable String qrcId) {
        QrcIdDTO qrcIdDTO = qrcIdService.markQrcIdAsUsed(qrcId);
        return ResponseEntity.ok(qrcIdDTO);
    }
}