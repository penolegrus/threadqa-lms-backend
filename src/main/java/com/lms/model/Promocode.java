package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "promocodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "is_percentage", nullable = false)
    private Boolean isPercentage;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "current_uses", nullable = false)
    private Integer currentUses;

    @Column(name = "valid_from", nullable = false)
    private ZonedDateTime validFrom;

    @Column(name = "valid_until")
    private ZonedDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}