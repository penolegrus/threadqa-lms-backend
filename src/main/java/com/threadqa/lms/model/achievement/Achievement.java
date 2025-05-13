package com.threadqa.lms.model.achievement;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "achievement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AchievementType type;

    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public enum AchievementType {
        COURSE_COMPLETION,
        COURSE_ENROLLMENT,
        TEST_COMPLETION,
        HOMEWORK_COMPLETION,
        PERFECT_SCORE,
        STREAK,
        FORUM_PARTICIPATION,
        PROFILE_COMPLETION,
        REFERRAL,
        SPECIAL
    }
}
