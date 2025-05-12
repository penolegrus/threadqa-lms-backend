package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_question_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_submission_id", nullable = false)
    private TestSubmission testSubmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_question_id", nullable = false)
    private TestQuestion testQuestion;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "code_answer", columnDefinition = "TEXT")
    private String codeAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}