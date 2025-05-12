package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_matching_pairs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestMatchingPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion question;

    @Column(name = "left_text", nullable = false, columnDefinition = "TEXT")
    private String left;

    @Column(name = "right_text", nullable = false, columnDefinition = "TEXT")
    private String right;
}