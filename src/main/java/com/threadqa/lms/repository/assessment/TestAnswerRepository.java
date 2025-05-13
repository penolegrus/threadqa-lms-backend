package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.TestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing TestAnswer entities.
 */
@Repository
public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {
    
    /**
     * Find all answers for a specific test submission.
     *
     * @param submissionId the ID of the test submission
     * @return list of test answers
     */
    List<TestAnswer> findByTestSubmissionId(Long submissionId);
    
    /**
     * Find all answers for a specific question in a test submission.
     *
     * @param submissionId the ID of the test submission
     * @param questionId the ID of the test question
     * @return list of test answers
     */
    List<TestAnswer> findByTestSubmissionIdAndTestQuestionId(Long submissionId, Long questionId);
    
    /**
     * Find an answer for a specific option in a test submission.
     *
     * @param submissionId the ID of the test submission
     * @param optionId the ID of the test option
     * @return optional test answer
     */
    Optional<TestAnswer> findByTestSubmissionIdAndTestOptionId(Long submissionId, Long optionId);
    
    /**
     * Check if a specific option was selected in a test submission.
     *
     * @param submissionId the ID of the test submission
     * @param optionId the ID of the test option
     * @return true if the option was selected, false otherwise
     */
    boolean existsByTestSubmissionIdAndTestOptionId(Long submissionId, Long optionId);
    
    /**
     * Delete all answers for a specific test submission.
     *
     * @param submissionId the ID of the test submission
     */
    void deleteByTestSubmissionId(Long submissionId);
    
    /**
     * Count the number of correct answers in a test submission.
     *
     * @param submissionId the ID of the test submission
     * @return the count of correct answers
     */
    @Query("SELECT COUNT(ta) FROM TestAnswer ta WHERE ta.testSubmission.id = :submissionId AND ta.isCorrect = true")
    long countCorrectAnswersBySubmissionId(@Param("submissionId") Long submissionId);
    
    /**
     * Count the total number of answers in a test submission.
     *
     * @param submissionId the ID of the test submission
     * @return the total count of answers
     */
    long countByTestSubmissionId(Long submissionId);
}
