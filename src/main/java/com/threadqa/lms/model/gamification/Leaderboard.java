package com.threadqa.lms.model.gamification;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "leaderboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "leaderboard_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaderboardType leaderboardType;

    @Column(name = "time_period", nullable = false)
    @Enumerated(EnumType.STRING)
    private TimePeriod timePeriod;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public enum LeaderboardType {
        POINTS, COURSES_COMPLETED, TESTS_COMPLETED, PERFECT_SCORES,
        LOGIN_STREAK, FORUM_ACTIVITY, REFERRALS
    }

    public enum TimePeriod {
        DAILY, WEEKLY, MONTHLY, ALL_TIME
    }
}
