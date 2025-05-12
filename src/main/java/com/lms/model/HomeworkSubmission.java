package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "homework_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(name = "programming_language")
    private String programmingLanguage;

    @Column(name = "github_link")
    private String githubLink;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "submitted_at", nullable = false)
    private ZonedDateTime submittedAt;

    @Column(name = "reviewed_at")
    private ZonedDateTime reviewedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<HomeworkChatMessage> chatMessages = new ArrayList<>();

    public enum SubmissionStatus {
        NOT_STARTED, IN_PROGRESS, SUBMITTED, REVIEWED, NEEDS_REVISION, ACCEPTED, REJECTED
    }
}