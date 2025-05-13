package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.QuizQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionAnswerRepository extends JpaRepository<QuizQuestionAnswer, Long> {

    @Query("SELECT qqa FROM QuizQuestionAnswer qqa WHERE qqa.quizAnswer.id = :quizAnswerId")
    List<QuizQuestionAnswer> findByQuizAnswerId(@Param("quizAnswerId") Long quizAnswerId);

    @Query("SELECT qqa FROM QuizQuestionAnswer qqa WHERE qqa.quizAnswer.id = :quizAnswerId AND qqa.question.id = :questionId")
    QuizQuestionAnswer findByQuizAnswerIdAndQuestionId(@Param("quizAnswerId") Long quizAnswerId, @Param("questionId") Long questionId);
}
