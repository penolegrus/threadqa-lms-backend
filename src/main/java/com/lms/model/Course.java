package com.lms.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    private String duration;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @ElementCollection
    @CollectionTable(
            name = "course_skills",
            joinColumns = @JoinColumn(name = "course_id")
    )
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "course_instructors",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> instructors = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Topic> topics = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    public enum CourseStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}