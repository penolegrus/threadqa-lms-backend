package com.threadqa.lms.service.payment;

import com.threadqa.lms.dto.payment.Qr;
import com.threadqa.lms.dto.payment.QrResponse;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.payment.QrcId;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.payment.QrcIdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {

    private final RestTemplate restTemplate;
    private final QrcIdRepository qrcIdRepository;
    private final JwtService jwtService;
    
    @Value("${payment.api.url}")
    private String paymentApiUrl;
    
    @Value("${payment.callback.url}")
    private String callbackUrl;
    
    @Value("${payment.redirect.url}")
    private String redirectUrl;
    
    /**
     * Создает QR-код для оплаты курса
     * 
     * @param user пользователь
     * @param course курс
     * @return ответ с QR-кодом
     */
    public QrResponse createQrCode(User user, Course course) {
        log.info("Creating QR code for user {} and course {}", user.getEmail(), course.getTitle());
        
        // Создаем запрос на генерацию QR-кода
        Qr qrRequest = Qr.builder()
                .name(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .amount(course.getPrice())
                .paymentPurpose("Оплата курса: " + course.getTitle())
                .ttl(3600) // Время жизни QR-кода в секундах (1 час)
                .redirectUrl(redirectUrl)
                .paymentCallbackUrl(callbackUrl)
                .build();
        
        // Подготавливаем заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Создаем JWT токен из запроса
        String jwtToken = jwtService.encode(qrRequest);
        
        // Отправляем запрос в платежную систему
        QrResponse response = restTemplate.postForObject(
                paymentApiUrl + "/qr",
                new HttpEntity<>(jwtToken, headers),
                QrResponse.class
        );
        
        if (response != null && response.getQrcId() != null) {
            // Сохраняем информацию о QR-коде в базу данных
            QrcId qrcId = new QrcId();
            qrcId.setQrcId(response.getQrcId());
            qrcId.setUser(user);
            qrcId.setCourse(course);
            qrcId.setAmount(course.getPrice());
            qrcId.setCreatedAt(LocalDateTime.now());
            qrcId.setExpiredAt(LocalDateTime.now().plusHours(1));
            qrcId.setStatus("PENDING");
            
            qrcIdRepository.save(qrcId);
            log.info("QR code created successfully with qrcId: {}", response.getQrcId());
        } else {
            log.error("Failed to create QR code");
        }
        
        return response;
    }
}
