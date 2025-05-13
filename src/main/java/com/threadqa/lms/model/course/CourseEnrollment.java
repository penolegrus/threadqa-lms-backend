package com.threadqa.lms.model.course;

import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "course_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false)
    private ZonedDateTime enrolledAt;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @Column(name = "progress", nullable = false)
    private Double progress;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}