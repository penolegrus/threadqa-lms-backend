package com.threadqa.lms.service.payment;

import com.threadqa.lms.dto.course.CourseEnrollmentRequest;
import com.threadqa.lms.dto.payment.CoursePurchaseRequest;
import com.threadqa.lms.dto.payment.CoursePurchaseResponse;
import com.threadqa.lms.dto.payment.QrResponse;
import com.threadqa.lms.dto.payment.Qr;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.payment.Payment;
import com.threadqa.lms.model.payment.QrcId;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.payment.QrcIdRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.course.CourseEnrollmentService;
import com.threadqa.lms.service.notification.NotificationService;
import com.threadqa.lms.service.promo.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePurchaseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final PaymentService paymentService;
    private final CourseEnrollmentService courseEnrollmentService;
    private final NotificationService notificationService;
    private final PromoCodeService promoCodeService;
    private final QrcIdRepository qrcIdRepository;

    /**
     * Обрабатывает запрос на покупку курса
     *
     * @param request запрос на покупку курса
     * @param userId ID пользователя
     * @return ответ с информацией о покупке
     */
    @Transactional
    public CoursePurchaseResponse purchaseCourse(CoursePurchaseRequest request, Long userId) {
        // Получаем пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получаем курс
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверяем, что курс доступен для покупки
        if (!course.getIsPublished()) {
            throw new BadRequestException("Course is not available for purchase");
        }

        // Проверяем, не записан ли пользователь уже на курс
        Optional<CourseEnrollment> existingEnrollment = courseEnrollmentRepository.findByUserIdAndCourseId(userId, course.getId());
        if (existingEnrollment.isPresent()) {
            throw new BadRequestException("User is already enrolled in this course");
        }

        // Рассчитываем стоимость с учетом промокода
        BigDecimal originalPrice = new BigDecimal(course.getPrice().toString());
        BigDecimal finalPrice = originalPrice;
        BigDecimal discountAmount = BigDecimal.ZERO;
        String promoCodeUsed = null;

        // Применяем промокод, если он указан
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            try {
                var promoValidation = promoCodeService.validatePromoCode(request.getPromoCode(), course.getId(), userId);
                if (promoValidation.getIsValid()) {
                    // Преобразуем Double в BigDecimal
                    discountAmount = new BigDecimal(promoValidation.getDiscountAmount().toString());
                    finalPrice = originalPrice.subtract(discountAmount);
                    promoCodeUsed = request.getPromoCode();
                }
            } catch (Exception e) {
                log.warn("Failed to apply promo code: {}", e.getMessage());
                // Продолжаем без промокода
            }
        }

        // Определяем метод оплаты
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "QR_CODE";

        // Создаем платеж в зависимости от метода оплаты
        if ("QR_CODE".equals(paymentMethod)) {
            // Генерируем QR-код для оплаты
            QrResponse qrResponse = generateQrCode(user, course, finalPrice);

            // Сохраняем информацию о QR-коде
            QrcId qrcId = QrcId.builder()
                    .qrcId(qrResponse.getQrcId())
                    .user(user)
                    .course(course)
                    .createdAt(ZonedDateTime.now())
                    .build();
            qrcIdRepository.save(qrcId);

            // Создаем платеж со статусом PENDING
            Payment payment = Payment.builder()
                    .user(user)
                    .course(course)
                    .amount(finalPrice)
                    .currency("RUB")
                    .status("PENDING")
                    .paymentMethod(paymentMethod)
                    .invoiceNumber(generateInvoiceNumber())
                    .paymentDescription("Purchase of course: " + course.getTitle())
                    .billingAddress(request.getBillingAddress())
                    .billingCity(request.getBillingCity())
                    .billingCountry(request.getBillingCountry())
                    .billingPostalCode(request.getBillingPostalCode())
                    .discountAmount(discountAmount)
                    .promoCode(promoCodeUsed)
                    .createdAt(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentService.savePayment(payment);

            // Отправляем уведомление о создании платежа
            notificationService.createNotification(
                    userId,
                    "Payment Created",
                    "Your payment for course '" + course.getTitle() + "' has been created. Please complete the payment.",
                    "PAYMENT",
                    "/payments/" + savedPayment.getId()
            );

            // Возвращаем ответ с информацией о QR-коде
            return CoursePurchaseResponse.builder()
                    .paymentId(savedPayment.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .amount(finalPrice)
                    .originalPrice(originalPrice)
                    .discountAmount(discountAmount)
                    .currency("RUB")
                    .status("PENDING")
                    .paymentMethod(paymentMethod)
                    .invoiceNumber(savedPayment.getInvoiceNumber())
                    .promoCodeUsed(promoCodeUsed)
                    .accessGranted(false)
                    .qrResponse(qrResponse)
                    .build();
        } else if ("FREE".equals(paymentMethod) && finalPrice.compareTo(BigDecimal.ZERO) == 0) {
            // Если курс бесплатный или стал бесплатным после применения промокода
            
            // Создаем платеж со статусом COMPLETED
            Payment payment = Payment.builder()
                    .user(user)
                    .course(course)
                    .amount(BigDecimal.ZERO)
                    .currency("RUB")
                    .status("COMPLETED")
                    .paymentMethod("FREE")
                    .transactionId(UUID.randomUUID().toString())
                    .paymentDate(LocalDateTime.now())
                    .invoiceNumber(generateInvoiceNumber())
                    .paymentDescription("Free enrollment to course: " + course.getTitle())
                    .discountAmount(discountAmount)
                    .promoCode(promoCodeUsed)
                    .createdAt(LocalDateTime.now())
                    .build();

           Payment savedPayment = paymentService.savePayment(payment);

            // Записываем пользователя на курс
            CourseEnrollmentRequest enrollmentRequest = new CourseEnrollmentRequest();
            enrollmentRequest.setCourseId(course.getId());
            enrollmentRequest.setUserId(userId);
            courseEnrollmentService.enrollInCourse(enrollmentRequest, userId);

            // Отправляем уведомление о успешной записи на курс
            notificationService.createNotification(
                    userId,
                    "Course Enrollment",
                    "You have been successfully enrolled in the course '" + course.getTitle() + "'.",
                    "ENROLLMENT",
                    "/courses/" + course.getId()
            );

            // Возвращаем ответ с информацией о бесплатной записи
            return CoursePurchaseResponse.builder()
                    .paymentId(savedPayment.getId())
                    .courseId(course.getId())
                    .courseTitle(course.getTitle())
                    .amount(BigDecimal.ZERO)
                    .originalPrice(originalPrice)
                    .discountAmount(discountAmount)
                    .currency("RUB")
                    .status("COMPLETED")
                    .paymentMethod("FREE")
                    .transactionId(savedPayment.getTransactionId())
                    .paymentDate(savedPayment.getPaymentDate())
                    .invoiceNumber(savedPayment.getInvoiceNumber())
                    .promoCodeUsed(promoCodeUsed)
                    .accessGranted(true)
                    .build();
        } else {
            // Другие методы оплаты (можно добавить в будущем)
            throw new BadRequestException("Payment method not supported: " + paymentMethod);
        }
    }

    /**
     * Генерирует QR-код для оплаты
     * 
     * @param user пользователь
     * @param course курс
     * @param amount сумма платежа
     * @return ответ с QR-кодом
     */
    private QrResponse generateQrCode(User user, Course course, BigDecimal amount) {
        // Создаем запрос на генерацию QR-кода
        Qr qrRequest = Qr.builder()
                .name(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .amount(amount.intValue())
                .paymentPurpose("Payment for course: " + course.getTitle())
                .ttl(3600) // Время жизни QR-кода в секундах (1 час)
                .redirectUrl("/courses/" + course.getId() + "/success")
                .paymentCallbackUrl("/api/payments/callback")
                .build();

        // В реальной системе здесь был бы вызов внешнего API для генерации QR-кода
        // Для примера создаем заглушку
        QrResponse qrResponse = new QrResponse();
        qrResponse.setQrcId(UUID.randomUUID().toString());
        qrResponse.setPayload("https://payment.example.com/qr/" + qrResponse.getQrcId());
        qrResponse.setIat(System.currentTimeMillis() / 1000);
        
        return qrResponse;
    }

    /**
     * Генерирует уникальный номер счета
     *
     * @return номер счета
     */
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
