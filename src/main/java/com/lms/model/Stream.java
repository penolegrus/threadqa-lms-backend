package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "streams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Column(name = "stream_key", nullable = false, unique = true)
    private String streamKey;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(name = "scheduled_start_time")
    private ZonedDateTime scheduledStartTime;

    @Column(name = "actual_start_time")
    private ZonedDateTime actualStartTime;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    @Column(name = "is_live", nullable = false)
    private Boolean isLive;

    @Column(name = "is_recorded", nullable = false)
    private Boolean isRecorded;

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "stream", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StreamChatMessage> chatMessages = new HashSet<>();

    @OneToMany(mappedBy = "stream", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StreamViewer> viewers = new HashSet<>();
}