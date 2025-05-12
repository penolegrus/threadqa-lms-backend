package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;
}