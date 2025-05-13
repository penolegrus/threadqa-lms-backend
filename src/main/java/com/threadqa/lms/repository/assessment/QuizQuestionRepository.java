package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    @Query("SELECT q FROM QuizQuestion q WHERE q.quiz.id = :quizId ORDER BY q.orderIndex")
    List<QuizQuestion> findByQuizIdOrderByOrderIndex(@Param("quizId") Long quizId);

    void deleteByQuizId(Long quizId);
}
