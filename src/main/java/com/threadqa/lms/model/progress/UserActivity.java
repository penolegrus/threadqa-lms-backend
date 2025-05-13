package com.threadqa.lms.model.progress;

import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public enum ActivityType {
        LOGIN, LOGOUT, VIEW_COURSE, ENROLL_COURSE, COMPLETE_COURSE, 
        VIEW_TOPIC, COMPLETE_TOPIC, START_TEST, COMPLETE_TEST,
        SUBMIT_HOMEWORK, RECEIVE_CERTIFICATE, PAYMENT, REFUND,
        UPDATE_PROFILE, CHANGE_PASSWORD, INVITE_FRIEND
    }
}
