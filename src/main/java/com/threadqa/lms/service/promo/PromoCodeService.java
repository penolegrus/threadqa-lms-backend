package com.threadqa.lms.service.promo;

import com.threadqa.lms.dto.promocode.PromoCodeRequest;
import com.threadqa.lms.dto.promocode.PromoCodeResponse;
import com.threadqa.lms.dto.promocode.PromoCodeValidationRequest;
import com.threadqa.lms.dto.promocode.PromoCodeValidationResponse;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.PromoCodeMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.promo.PromoCode;
import com.threadqa.lms.model.promo.PromoCodeUsage;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.promo.PromoCodeRepository;
import com.threadqa.lms.repository.promo.PromoCodeUsageRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        return promoCodes.stream()
                .map(promoCodeMapper::toPromoCodeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse getPromoCode(Long promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));
        return promoCodeMapper.toPromoCodeResponse(promoCode);
    }

    @Transactional
    public PromoCodeResponse createPromoCode(PromoCodeRequest request) {
        // Проверка уникальности кода
        if (promoCodeRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Promo code already exists");
        }

        // Проверка наличия хотя бы одного типа скидки
        if ((request.getDiscountPercent() == null || request.getDiscountPercent() == 0) && 
            (request.getDiscountAmount() == null || request.getDiscountAmount() == 0)) {
            throw new BadRequestException("Either discount percent or discount amount must be provided");
        }

        // Получение применимых курсов
        Set<Course> applicableCourses = new HashSet<>();
        if (request.getApplicableCourseIds() != null && !request.getApplicableCourseIds().isEmpty()) {
            for (Long courseId : request.getApplicableCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
                applicableCourses.add(course);
            }
        }

        PromoCode promoCode = PromoCode.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .isActive(request.getIsActive())
                .maxUses(request.getMaxUses())
                .currentUses(0)
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .applicableCourses(applicableCourses)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);
        return promoCodeMapper.toPromoCodeResponse(savedPromoCode);
    }

    @Transactional
    public PromoCodeResponse updatePromoCode(Long promoCodeId, PromoCodeRequest request) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));

        // Проверка уникальности кода, если он изменился
        if (!promoCode.getCode().equals(request.getCode()) && 
            promoCodeRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Promo code already exists");
        }

        // Проверка наличия хотя бы одного типа скидки
        if ((request.getDiscountPercent() == null || request.getDiscountPercent() == 0) && 
            (request.getDiscountAmount() == null || request.getDiscountAmount() == 0)) {
            throw new BadRequestException("Either discount percent or discount amount must be provided");
        }

        // Обновление применимых курсов
        Set<Course> applicableCourses = new HashSet<>();
        if (request.getApplicableCourseIds() != null) {
            for (Long courseId : request.getApplicableCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
                applicableCourses.add(course);
            }
            promoCode.setApplicableCourses(applicableCourses);
        }

        promoCode.setCode(request.getCode());
        promoCode.setDescription(request.getDescription());
        promoCode.setDiscountPercent(request.getDiscountPercent());
        promoCode.setDiscountAmount(request.getDiscountAmount());
        promoCode.setIsActive(request.getIsActive());
        promoCode.setMaxUses(request.getMaxUses());
        promoCode.setValidFrom(request.getValidFrom());
        promoCode.setValidTo(request.getValidTo());
        promoCode.setUpdatedAt(ZonedDateTime.now());

        PromoCode updatedPromoCode = promoCodeRepository.save(promoCode);
        return promoCodeMapper.toPromoCodeResponse(updatedPromoCode);
    }

    @Transactional
    public void deletePromoCode(Long promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));

        // Удаление связанных записей об использовании
        List<PromoCodeUsage> usages = promoCodeUsageRepository.findByPromoCode(promoCode);
        promoCodeUsageRepository.deleteAll(usages);

        // Удаление промо-кода
        promoCodeRepository.delete(promoCode);
    }

    @Transactional(readOnly = true)
    public PromoCodeValidationResponse validatePromoCode(PromoCodeValidationRequest request, Long currentUserId) {
        // Получение промо-кода
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCode(request.getCode());
        
        if (promoCodeOpt.isEmpty()) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("Promo code not found")
                    .build();
        }
        
        PromoCode promoCode = promoCodeOpt.get();
        
        // Проверка активности
        if (!promoCode.getIsActive()) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("Promo code is not active")
                    .build();
        }
        
        // Проверка срока действия
        ZonedDateTime now = ZonedDateTime.now();
        if (promoCode.getValidFrom().isAfter(now)) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("Promo code is not yet valid")
                    .build();
        }
        
        if (promoCode.getValidTo() != null && promoCode.getValidTo().isBefore(now)) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("Promo code has expired")
                    .build();
        }
        
        // Проверка максимального количества использований
        if (promoCode.getMaxUses() != null && promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("Promo code has reached maximum uses")
                    .build();
        }
        
        // Получение курса
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Проверка применимости к курсу
        if (!promoCode.getApplicableCourses().isEmpty() && !promoCode.getApplicableCourses().contains(course)) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("Promo code is not applicable to this course")
                    .build();
        }
        
        // Проверка, не использовал ли пользователь уже этот промо-код для этого курса
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Optional<PromoCodeUsage> existingUsage = promoCodeUsageRepository.findByPromoCodeAndUserAndCourse(promoCode, user, course);
        if (existingUsage.isPresent()) {
            return PromoCodeValidationResponse.builder()
                    .isValid(false)
                    .message("You have already used this promo code for this course")
                    .build();
        }
        
        // Расчет скидки
        Double originalPrice = course.getPrice();
        Double discountedPrice = originalPrice;
        
        if (promoCode.getDiscountPercent() != null && promoCode.getDiscountPercent() > 0) {
            discountedPrice = originalPrice * (1 - promoCode.getDiscountPercent() / 100.0);
        }
        
        if (promoCode.getDiscountAmount() != null && promoCode.getDiscountAmount() > 0) {
            discountedPrice = Math.max(0, discountedPrice - promoCode.getDiscountAmount());
        }
        
        return PromoCodeValidationResponse.builder()
                .isValid(true)
                .code(promoCode.getCode())
                .description(promoCode.getDescription())
                .discountPercent(promoCode.getDiscountPercent())
                .discountAmount(promoCode.getDiscountAmount())
                .originalPrice(originalPrice)
                .discountedPrice(discountedPrice)
                .message("Promo code is valid")
                .build();
    }

    @Transactional
    public void usePromoCode(String code, Long courseId, Long userId) {
        // Получение промо-кода
        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));
        
        // Получение пользователя и курса
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Проверка, не использовал ли пользователь уже этот промо-код для этого курса
        Optional<PromoCodeUsage> existingUsage = promoCodeUsageRepository.findByPromoCodeAndUserAndCourse(promoCode, user, course);
        if (existingUsage.isPresent()) {
            throw new BadRequestException("You have already used this promo code for this course");
        }
        
        // Расчет скидки
        Double originalPrice = course.getPrice();
        Double discountAmount = 0.0;
        
        if (promoCode.getDiscountPercent() != null && promoCode.getDiscountPercent() > 0) {
            discountAmount += originalPrice * (promoCode.getDiscountPercent() / 100.0);
        }
        
        if (promoCode.getDiscountAmount() != null && promoCode.getDiscountAmount() > 0) {
            discountAmount += promoCode.getDiscountAmount();
        }
        
        // Ограничение скидки ценой курса
        discountAmount = Math.min(discountAmount, originalPrice);
        
        // Создание записи об использовании
        PromoCodeUsage usage = PromoCodeUsage.builder()
                .promoCode(promoCode)
                .user(user)
                .course(course)
                .discountAmount(discountAmount)
                .usedAt(ZonedDateTime.now())
                .build();
        
        promoCodeUsageRepository.save(usage);
        
        // Увеличение счетчика использований
        promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
        promoCodeRepository.save(promoCode);
    }

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getValidPromoCodesForCourse(Long courseId) {
        ZonedDateTime now = ZonedDateTime.now();
        List<PromoCode> promoCodes = promoCodeRepository.findValidPromoCodesForCourseAt(courseId, now);
        
        return promoCodes.stream()
                .map(promoCodeMapper::toPromoCodeResponse)
                .collect(Collectors.toList());
    }
}
