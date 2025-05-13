package com.threadqa.lms.model.referral;

import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "referral_invitations")
public class ReferralInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_code_id", nullable = false)
    private ReferralCode referralCode;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;
    
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_user_id")
    private User registeredUser;
    
    @Column(name = "is_converted", nullable = false)
    private boolean isConverted;
    
    @PrePersist
    protected void onCreate() {
        invitedAt = LocalDateTime.now();
        if (isConverted == false) {
            isConverted = false;
        }
    }
}
