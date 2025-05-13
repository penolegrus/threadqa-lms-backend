package com.threadqa.lms.model.gamification;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "badge_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BadgeType badgeType;

    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @Column(name = "points_reward")
    private Integer pointsReward;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public enum BadgeType {
        COURSE_COMPLETION, TOPIC_MASTERY, QUIZ_MASTER, TEST_EXPERT,
        HOMEWORK_DILIGENT, LOGIN_STREAK, FORUM_CONTRIBUTOR, COMMENT_ENTHUSIAST,
        REFERRAL_CHAMPION, ACHIEVEMENT_COLLECTOR, PERFECT_SCORE, EARLY_BIRD,
        NIGHT_OWL, WEEKEND_WARRIOR, COURSE_CREATOR
    }
}
