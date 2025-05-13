package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.TestOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing TestOption entities.
 * Test options are the possible answers for multiple-choice and checkbox-type questions.
 */
@Repository
public interface TestOptionRepository extends JpaRepository<TestOption, Long> {
    
    /**
     * Find all options for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return list of test options
     */
    List<TestOption> findByTestQuestionId(Long questionId);
    
    /**
     * Find all correct options for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return list of correct test options
     */
    List<TestOption> findByTestQuestionIdAndCorrectTrue(Long questionId);
    
    /**
     * Find all incorrect options for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return list of incorrect test options
     */
    List<TestOption> findByTestQuestionIdAndCorrectFalse(Long questionId);
    
    /**
     * Count the number of options for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return the count of options
     */
    long countByTestQuestionId(Long questionId);
    
    /**
     * Count the number of correct options for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return the count of correct options
     */
    long countByTestQuestionIdAndCorrectTrue(Long questionId);
    
    /**
     * Delete all options for a specific test question.
     *
     * @param questionId the ID of the test question
     */
    void deleteByTestQuestionId(Long questionId);
    
    /**
     * Find options by display order for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return list of options ordered by display order
     */
    @Query("SELECT to FROM TestOption to WHERE to.testQuestion.id = :questionId ORDER BY to.displayOrder")
    List<TestOption> findByTestQuestionIdOrderByDisplayOrder(@Param("questionId") Long questionId);
    
    /**
     * Find options containing specific text for a test question.
     *
     * @param questionId the ID of the test question
     * @param text the text to search for
     * @return list of options containing the specified text
     */
    @Query("SELECT to FROM TestOption to WHERE to.testQuestion.id = :questionId AND LOWER(to.optionText) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<TestOption> findByTestQuestionIdAndOptionTextContaining(@Param("questionId") Long questionId, @Param("text") String text);
}
