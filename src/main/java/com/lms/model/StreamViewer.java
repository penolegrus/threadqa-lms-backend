package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "stream_viewers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamViewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private ZonedDateTime joinedAt;

    @Column(name = "left_at")
    private ZonedDateTime leftAt;

    @Column(name = "total_watch_time_seconds")
    private Long totalWatchTimeSeconds;
}