package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "referral_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_code_id", nullable = false)
    private ReferralCode referralCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id", nullable = false)
    private User referredUser;

    @Column(name = "registered_at", nullable = false)
    private ZonedDateTime registeredAt;

    @Column(name = "is_converted", nullable = false)
    private Boolean isConverted;

    @Column(name = "converted_at")
    private ZonedDateTime convertedAt;
}