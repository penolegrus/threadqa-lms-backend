package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing TestCase entities.
 * Test cases are used for programming questions where code execution needs to be tested.
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    /**
     * Find all test cases for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return list of test cases
     */
    List<TestCase> findByTestQuestionId(Long questionId);
    
    /**
     * Find all visible test cases for a specific test question.
     * Visible test cases are shown to students before submission.
     *
     * @param questionId the ID of the test question
     * @return list of visible test cases
     */
    List<TestCase> findByTestQuestionIdAndVisibleTrue(Long questionId);
    
    /**
     * Find all hidden test cases for a specific test question.
     * Hidden test cases are only used for evaluation and not shown to students.
     *
     * @param questionId the ID of the test question
     * @return list of hidden test cases
     */
    List<TestCase> findByTestQuestionIdAndVisibleFalse(Long questionId);
    
    /**
     * Count the number of test cases for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return the count of test cases
     */
    long countByTestQuestionId(Long questionId);
    
    /**
     * Delete all test cases for a specific test question.
     *
     * @param questionId the ID of the test question
     */
    void deleteByTestQuestionId(Long questionId);
    
    /**
     * Find test cases by difficulty level for a specific test question.
     *
     * @param questionId the ID of the test question
     * @param difficulty the difficulty level
     * @return list of test cases with the specified difficulty
     */
    @Query("SELECT tc FROM TestCase tc WHERE tc.testQuestion.id = :questionId AND tc.difficulty = :difficulty")
    List<TestCase> findByTestQuestionIdAndDifficulty(@Param("questionId") Long questionId, @Param("difficulty") String difficulty);
}
