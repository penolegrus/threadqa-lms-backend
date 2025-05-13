package com.threadqa.lms.model.user;

import com.threadqa.lms.model.achievement.Achievement;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "earned_at", nullable = false)
    private ZonedDateTime earnedAt;

    @Column(name = "progress", nullable = false)
    private Integer progress;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "is_notified", nullable = false)
    private Boolean isNotified;
}
