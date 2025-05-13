package com.threadqa.lms.model.gamification;

import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Column(name = "current_points", nullable = false)
    private Integer currentPoints;

    @Column(name = "points_to_next_level", nullable = false)
    private Integer pointsToNextLevel;

    @Column(name = "achieved_at", nullable = false)
    private ZonedDateTime achievedAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
