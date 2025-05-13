package com.threadqa.lms.model.gamification;

import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "point_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType pointType;

    @Column(name = "description")
    private String description;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public enum PointType {
        COURSE_COMPLETION, TOPIC_COMPLETION, QUIZ_COMPLETION, TEST_COMPLETION,
        HOMEWORK_SUBMISSION, DAILY_LOGIN, STREAK_BONUS, COMMENT, FORUM_POST,
        REFERRAL, ACHIEVEMENT_UNLOCK, FIRST_COURSE, PERFECT_SCORE
    }
}
