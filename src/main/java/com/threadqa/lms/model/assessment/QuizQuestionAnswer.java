package com.threadqa.lms.model.assessment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quiz_question_answers")
public class QuizQuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_answer_id", nullable = false)
    private QuizAnswer quizAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(length = 1000)
    private String textAnswer;

    @Column(nullable = false)
    private Integer pointsEarned;

    @ManyToMany
    @JoinTable(
        name = "quiz_question_answer_options",
        joinColumns = @JoinColumn(name = "question_answer_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private List<QuizOption> selectedOptions = new ArrayList<>();
}
