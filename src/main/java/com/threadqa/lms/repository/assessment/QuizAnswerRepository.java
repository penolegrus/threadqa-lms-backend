package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.QuizAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId ORDER BY qa.startedAt DESC")
    List<QuizAnswer> findByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.user.id = :userId ORDER BY qa.startedAt DESC")
    Page<QuizAnswer> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.quiz.id = :quizId ORDER BY qa.startedAt DESC")
    Page<QuizAnswer> findByQuizId(@Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL ORDER BY qa.completedAt DESC")
    Optional<QuizAnswer> findLatestCompletedByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query("SELECT COUNT(qa) FROM QuizAnswer qa WHERE qa.quiz.id = :quizId AND qa.isPassed = true")
    Long countPassedByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT AVG(qa.score) FROM QuizAnswer qa WHERE qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL")
    Double getAverageScoreByQuizId(@Param("quizId") Long quizId);
}
