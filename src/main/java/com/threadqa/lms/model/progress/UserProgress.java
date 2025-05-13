package com.threadqa.lms.model.progress;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "progress_percentage")
    private Double progressPercentage;

    @Column(name = "time_spent_seconds")
    private Long timeSpentSeconds;

    @Column(name = "last_accessed_at")
    private ZonedDateTime lastAccessedAt;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public enum ContentType {
        VIDEO, DOCUMENT, QUIZ, TEST, HOMEWORK, DISCUSSION
    }
}
