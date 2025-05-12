package com.lms.service;

import com.lms.dto.payment.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.PaymentMapper;
import com.lms.model.*;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PromocodeRepository promocodeRepository;
    private final ReferralCodeRepository referralCodeRepository;
    private final ReferralRegistrationRepository referralRegistrationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Check if user is already enrolled
        courseEnrollmentRepository.findByCourseAndUser(course, user)
                .ifPresent(enrollment -> {
                    throw new IllegalStateException("User is already enrolled in this course");
                });

        BigDecimal finalAmount = request.getAmount();
        Promocode promocode = null;
        ReferralCode referralCode = null;

        // Apply promocode if provided
        if (request.getPromocode() != null && !request.getPromocode().isEmpty()) {
            promocode = promocodeRepository.findValidPromocode(request.getPromocode(), ZonedDateTime.now())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired promocode"));

            if (promocode.getIsPercentage()) {
                BigDecimal discountFactor = BigDecimal.valueOf(promocode.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
                BigDecimal discountAmount = request.getAmount().multiply(discountFactor);
                finalAmount = request.getAmount().subtract(discountAmount);
            } else {
                finalAmount = request.getAmount().subtract(promocode.getDiscountAmount());
                if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    finalAmount = BigDecimal.ZERO;
                }
            }
        }

        // Apply referral code if provided
        if (request.getReferralCode() != null && !request.getReferralCode().isEmpty()) {
            referralCode = referralCodeRepository.findByCodeAndIsActiveTrue(request.getReferralCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid referral code"));

            // Check if the referral code belongs to the user (can't use own referral code)
            if (referralCode.getUser().getId().equals(userId)) {
                throw new IllegalStateException("Cannot use your own referral code");
            }

            // Apply referral discount
            BigDecimal discountFactor = BigDecimal.valueOf(referralCode.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
            BigDecimal discountAmount = finalAmount.multiply(discountFactor);
            finalAmount = finalAmount.subtract(discountAmount);
        }

        // Create payment record
        Payment payment = Payment.builder()
                .user(user)
                .course(course)
                .paymentMethod(request.getPaymentMethod())
                .amount(finalAmount)
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .transactionId(generateTransactionId())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .promocode(promocode)
                .referralCode(referralCode)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // In a real implementation, we would integrate with a payment gateway here
        // For now, we'll simulate a successful payment

        return paymentMapper.toPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING state");
        }

        // Update payment status
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(ZonedDateTime.now());
        payment.setUpdatedAt(ZonedDateTime.now());

        // Update promocode usage if applicable
        if (payment.getPromocode() != null) {
            Promocode promocode = payment.getPromocode();
            promocode.setCurrentUses(promocode.getCurrentUses() + 1);
            promocodeRepository.save(promocode);
        }

        // Process referral if applicable
        if (payment.getReferralCode() != null) {
            processReferralPayment(payment);
        }

        // Enroll user in the course
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .course(payment.getCourse())
                .user(payment.getUser())
                .enrolledAt(ZonedDateTime.now())
                .build();

        courseEnrollmentRepository.save(enrollment);

        Payment updatedPayment = paymentRepository.save(payment);

        return paymentMapper.toPaymentResponse(updatedPayment);
    }

    private void processReferralPayment(Payment payment) {
        ReferralCode referralCode = payment.getReferralCode();
        User referrer = referralCode.getUser();
        User referred = payment.getUser();

        // Check if this is a new referral
        List<ReferralRegistration> existingRegistrations = referralRegistrationRepository.findByReferredUser(referred);
        if (existingRegistrations.isEmpty()) {
            // Create referral registration
            ReferralRegistration registration = ReferralRegistration.builder()
                    .referralCode(referralCode)
                    .referredUser(referred)
                    .registeredAt(ZonedDateTime.now())
                    .isConverted(true)
                    .convertedAt(ZonedDateTime.now())
                    .build();

            referralRegistrationRepository.save(registration);

            // Create payment for referrer reward
            Payment referrerPayment = Payment.builder()
                    .user(referrer)
                    .paymentMethod(PaymentMethod.BANK_TRANSFER) // Default method for rewards
                    .amount(referralCode.getReferrerRewardAmount())
                    .currency(payment.getCurrency())
                    .status(PaymentStatus.COMPLETED)
                    .transactionId(generateTransactionId())
                    .paymentDate(ZonedDateTime.now())
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();

            paymentRepository.save(referrerPayment);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Check if user is authorized to view this payment
        if (!payment.getUser().getId().equals(userId) && !isAdmin(userId)) {
            throw new AccessDeniedException("You are not authorized to view this payment");
        }

        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Payment> payments = paymentRepository.findByUser(user, pageable);

        return payments.map(paymentMapper::toPaymentResponse);
    }

    @Transactional
    public PromocodeResponse createPromocode(PromocodeRequest request) {
        Promocode promocode = Promocode.builder()
                .code(request.getCode())
                .discountPercentage(request.getIsPercentage() ? request.getDiscountPercentage() : null)
                .discountAmount(request.getIsPercentage() ? null : request.getDiscountAmount())
                .isPercentage(request.getIsPercentage())
                .maxUses(request.getMaxUses())
                .currentUses(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(request.getIsActive())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Promocode savedPromocode = promocodeRepository.save(promocode);

        return paymentMapper.toPromocodeResponse(savedPromocode);
    }

    @Transactional(readOnly = true)
    public List<PromocodeResponse> getAllPromocodes() {
        List<Promocode> promocodes = promocodeRepository.findAll();

        return promocodes.stream()
                .map(paymentMapper::toPromocodeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromocodeResponse> getActivePromocodes() {
        List<Promocode> promocodes = promocodeRepository.findAllActivePromocodes(ZonedDateTime.now());

        return promocodes.stream()
                .map(paymentMapper::toPromocodeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReferralCodeResponse createReferralCode(ReferralCodeRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already has an active referral code
        List<ReferralCode> existingCodes = referralCodeRepository.findByUserAndIsActiveTrue(user);
        if (!existingCodes.isEmpty()) {
            // Deactivate existing codes
            for (ReferralCode existingCode : existingCodes) {
                existingCode.setIsActive(false);
                existingCode.setUpdatedAt(ZonedDateTime.now());
                referralCodeRepository.save(existingCode);
            }
        }

        // Generate a unique referral code
        String code = generateReferralCode(user);

        ReferralCode referralCode = ReferralCode.builder()
                .code(code)
                .user(user)
                .discountPercentage(request.getDiscountPercentage())
                .referrerRewardAmount(request.getReferrerRewardAmount())
                .isActive(true)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        ReferralCode savedReferralCode = referralCodeRepository.save(referralCode);

        Long successfulReferrals = referralRegistrationRepository.countSuccessfulReferrals(userId);

        return paymentMapper.toReferralCodeResponse(savedReferralCode, successfulReferrals);
    }

    @Transactional(readOnly = true)
    public ReferralCodeResponse getUserReferralCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ReferralCode> referralCodes = referralCodeRepository.findByUserAndIsActiveTrue(user);

        if (referralCodes.isEmpty()) {
            throw new ResourceNotFoundException("User has no active referral code");
        }

        ReferralCode referralCode = referralCodes.get(0);
        Long successfulReferrals = referralRegistrationRepository.countSuccessfulReferrals(userId);

        return paymentMapper.toReferralCodeResponse(referralCode, successfulReferrals);
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateReferralCode(User user) {
        String baseCode = user.getUsername().substring(0, Math.min(user.getUsername().length(), 5)).toUpperCase();
        return baseCode + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
}