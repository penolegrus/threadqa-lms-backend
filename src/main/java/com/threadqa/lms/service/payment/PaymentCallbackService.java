package com.threadqa.lms.service.payment;

import com.threadqa.lms.dto.course.CourseEnrollmentRequest;
import com.threadqa.lms.dto.payment.Callback;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.payment.Payment;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.payment.QrcIdRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.course.CourseEnrollmentService;
import com.threadqa.lms.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final JwtService jwtService;
    private final QrcIdRepository qrcIdRepository;
    private final CourseEnrollmentService courseEnrollmentService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    /**
     * Обрабатывает callback от платежной системы
     * 
     * @param jwtToken JWT токен с информацией о платеже
     */
    @Transactional
    public void handleCallback(String jwtToken) {
        try {
            // Декодируем JWT в объект Callback
            Callback callback = jwtService.decode(jwtToken, Callback.class);
            log.info("Received payment callback for qrcId: {}, amount: {}", callback.getQrcId(), callback.getAmount());

            // Находим информацию о QR-коде
            qrcIdRepository.findByQrcId(callback.getQrcId())
                    .ifPresent(qrcId -> {
                        User user = qrcId.getUser();
                        Course course = qrcId.getCourse();
                        
                        // Проверяем, есть ли уже запись на курс
                        boolean alreadyEnrolled = courseEnrollmentService.getUserEnrollments(user.getId(), null)
                                .getContent().stream()
                                .anyMatch(enrollment -> enrollment.getCourseId().equals(course.getId()));
                        
                        if (alreadyEnrolled) {
                            log.info("User {} already has access to course {}", user.getEmail(), course.getId());
                            return;
                        }
                        
                        // Записываем пользователя на курс
                        CourseEnrollmentRequest enrollmentRequest = new CourseEnrollmentRequest();
                        enrollmentRequest.setCourseId(course.getId());
                        enrollmentRequest.setUserId(user.getId());
                        courseEnrollmentService.enrollInCourse(enrollmentRequest, user.getId());
                        
                        // Создаем запись о платеже
                        Payment payment = Payment.builder()
                                .user(user)
                                .course(course)
                                .amount(new BigDecimal(callback.getAmount())) // Преобразуем Integer в BigDecimal
                                .paymentMethod("QR_CODE")
                                .transactionId(callback.getTransactionId())
                                .status("COMPLETED")
                                .paymentDate(LocalDateTime.now()) // Используем LocalDateTime вместо ZonedDateTime
                                .build();
                        
                        paymentService.savePayment(payment);

                        // Отправляем уведомление об успешной оплате
                        notificationService.createNotification(
                                user.getId(),
                                "Покупка курса",
                                "Вы успешно приобрели курс \"" + course.getTitle() + "\" за " + callback.getAmount() + " руб.",
                                "PAYMENT",
                                "/courses/" + course.getId()
                        );

                        log.info("Successfully processed payment for user {} and course {}", 
                                user.getEmail(), course.getTitle());
                    });
        } catch (Exception e) {
            log.error("Failed to process payment callback: {}", jwtToken, e);
            throw new RuntimeException("Failed to process payment callback", e);
        }
    }
}
