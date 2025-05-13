package com.threadqa.lms.model.payment;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "qrc_ids")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrcId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String qrcId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;
}
