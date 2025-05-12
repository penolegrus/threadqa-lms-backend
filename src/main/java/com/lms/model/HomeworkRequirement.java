package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "homework_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requirement;
}