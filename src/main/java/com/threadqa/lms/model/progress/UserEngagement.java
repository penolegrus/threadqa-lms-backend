package com.threadqa.lms.model.progress;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_engagements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEngagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "engagement_date", nullable = false)
    private ZonedDateTime engagementDate;

    @Column(name = "session_duration_seconds")
    private Long sessionDurationSeconds;

    @Column(name = "page_views")
    private Integer pageViews;

    @Column(name = "interactions")
    private Integer interactions;

    @Column(name = "comments")
    private Integer comments;

    @Column(name = "questions_asked")
    private Integer questionsAsked;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
