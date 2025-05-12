package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "certificate_number", nullable = false, unique = true)
    private String certificateNumber;

    @Column(name = "verification_code", nullable = false, unique = true)
    private String verificationCode;

    @Column(name = "issue_date", nullable = false)
    private ZonedDateTime issueDate;

    @Column(name = "expiry_date")
    private ZonedDateTime expiryDate;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "revocation_date")
    private ZonedDateTime revocationDate;

    @Column(name = "certificate_url")
    private String certificateUrl;
}