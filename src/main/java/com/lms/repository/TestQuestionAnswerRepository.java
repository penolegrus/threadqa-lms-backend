package com.lms.repository;

import com.lms.model.TestQuestion;
import com.lms.model.TestQuestionAnswer;
import com.lms.model.TestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionAnswerRepository extends JpaRepository<TestQuestionAnswer, Long> {

    List<TestQuestionAnswer> findByTestSubmission(TestSubmission testSubmission);

    @Query("SELECT COUNT(tqa) FROM TestQuestionAnswer tqa WHERE tqa.testQuestion.id = :questionId AND tqa.isCorrect = true")
    Long countCorrectAnswersForQuestion(Long questionId);

    @Query("SELECT COUNT(tqa) FROM TestQuestionAnswer tqa WHERE tqa.testQuestion.id = :questionId")
    Long countTotalAnswersForQuestion(Long questionId);
}