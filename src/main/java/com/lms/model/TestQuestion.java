package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "code_template", columnDefinition = "TEXT")
    private String codeTemplate;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestMatchingPair> matchingPairs = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    public enum QuestionType {
        SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, MATCHING, CODE
    }
}