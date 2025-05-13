package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.promocode.PromoCodeResponse;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.promo.PromoCode;
import com.threadqa.lms.repository.promo.PromoCodeUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PromoCodeMapper {

    private final PromoCodeUsageRepository promoCodeUsageRepository;

    public PromoCodeResponse toPromoCodeResponse(PromoCode promoCode) {
        if (promoCode == null) {
            return null;
        }

        // Подсчет статистики использования
        Long usageCount = promoCodeUsageRepository.countByPromoCodeId(promoCode.getId());
        Double totalDiscountAmount = promoCodeUsageRepository.getTotalDiscountByPromoCodeId(promoCode.getId());

        // Преобразование применимых курсов
        List<PromoCodeResponse.CourseDTO> applicableCourses = promoCode.getApplicableCourses().stream()
                .map(this::toCourseDTO)
                .collect(Collectors.toList());

        return PromoCodeResponse.builder()
                .id(promoCode.getId())
                .code(promoCode.getCode())
                .description(promoCode.getDescription())
                .discountPercent(promoCode.getDiscountPercent())
                .discountAmount(promoCode.getDiscountAmount())
                .isActive(promoCode.getIsActive())
                .maxUses(promoCode.getMaxUses())
                .currentUses(promoCode.getCurrentUses())
                .validFrom(promoCode.getValidFrom())
                .validTo(promoCode.getValidTo())
                .applicableCourses(applicableCourses)
                .createdAt(promoCode.getCreatedAt())
                .updatedAt(promoCode.getUpdatedAt())
                .usageCount(usageCount != null ? usageCount.intValue() : 0)
                .totalDiscountAmount(totalDiscountAmount != null ? totalDiscountAmount : 0.0)
                .build();
    }

    private PromoCodeResponse.CourseDTO toCourseDTO(Course course) {
        return PromoCodeResponse.CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .build();
    }
}
