package com.threadqa.lms.dto.promocode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponse {

    private Long id;
    private String code;
    private String description;
    private Integer discountPercent;
    private Double discountAmount;
    private Boolean isActive;
    private Integer maxUses;
    private Integer currentUses;
    private ZonedDateTime validFrom;
    private ZonedDateTime validTo;
    private List<CourseDTO> applicableCourses;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Integer usageCount;
    private Double totalDiscountAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseDTO {
        private Long id;
        private String title;
    }
}
