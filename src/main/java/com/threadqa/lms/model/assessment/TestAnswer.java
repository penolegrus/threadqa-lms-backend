package com.threadqa.lms.model.assessment;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private TestSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion question;

    @ManyToMany
    @JoinTable(
            name = "test_answer_options",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private List<TestOption> selectedOptions = new ArrayList<>();

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "code_answer", columnDefinition = "TEXT")
    private String codeAnswer;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
