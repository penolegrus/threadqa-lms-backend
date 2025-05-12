package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "test_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "score")
    private Integer score;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt;

    @Column(name = "submitted_at")
    private ZonedDateTime submittedAt;

    @Column(name = "time_spent_seconds")
    private Long timeSpentSeconds;

    @OneToMany(mappedBy = "testSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TestQuestionAnswer> answers = new HashSet<>();
}