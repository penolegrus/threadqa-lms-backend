package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {

    @Query("SELECT o FROM QuizOption o WHERE o.question.id = :questionId ORDER BY o.orderIndex")
    List<QuizOption> findByQuestionIdOrderByOrderIndex(@Param("questionId") Long questionId);

    void deleteByQuestionId(Long questionId);
}
