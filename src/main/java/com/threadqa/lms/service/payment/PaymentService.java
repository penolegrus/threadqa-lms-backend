package com.threadqa.lms.service.payment;

import com.threadqa.lms.dto.payment.PaymentRequest;
import com.threadqa.lms.dto.payment.PaymentResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.PaymentMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.payment.Payment;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.payment.PaymentRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PaymentMapper paymentMapper;
    
    /**
     * Создает новую запись о платеже
     * 
     * @param paymentRequest данные платежа
     * @return информация о созданном платеже
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        User user = userRepository.findById(paymentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Course course = courseRepository.findById(paymentRequest.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        Payment payment = Payment.builder()
                .user(user)
                .course(course)
                .amount(paymentRequest.getAmount())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .transactionId(paymentRequest.getTransactionId())
                .status(paymentRequest.getStatus())
                .paymentDate(ZonedDateTime.now())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(savedPayment);
    }
    
    /**
     * Сохраняет платеж в базе данных
     * 
     * @param payment объект платежа
     * @return сохраненный платеж
     */
    @Transactional
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    /**
     * Получает платеж по ID
     * 
     * @param paymentId ID платежа
     * @return информация о платеже
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        return paymentMapper.toPaymentResponse(payment);
    }
    
    /**
     * Получает все платежи пользователя
     * 
     * @param userId ID пользователя
     * @param pageable параметры пагинации
     * @return страница с платежами пользователя
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<Payment> payments = paymentRepository.findByUser(user, pageable);
        return payments.map(paymentMapper::toPaymentResponse);
    }
    
    /**
     * Получает все платежи за курс
     * 
     * @param courseId ID курса
     * @param pageable параметры пагинации
     * @return страница с платежами за курс
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getCoursePayments(Long courseId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        Page<Payment> payments = paymentRepository.findByCourse(course, pageable);
        return payments.map(paymentMapper::toPaymentResponse);
    }
    
    /**
     * Обновляет статус платежа
     * 
     * @param paymentId ID платежа
     * @param status новый статус
     * @return обновленная информация о платеже
     */
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        
        return paymentMapper.toPaymentResponse(updatedPayment);
    }
}
