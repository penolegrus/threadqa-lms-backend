package com.threadqa.lms.model.assessment;

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

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
}