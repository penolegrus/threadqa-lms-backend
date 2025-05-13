package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.TestMatchingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing TestMatchingPair entities.
 * Matching pairs are used in matching-type questions where students need to match items from two columns.
 */
@Repository
public interface TestMatchingPairRepository extends JpaRepository<TestMatchingPair, Long> {
    
    /**
     * Find all matching pairs for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return list of matching pairs
     */
    List<TestMatchingPair> findByTestQuestionId(Long questionId);
    
    /**
     * Find a matching pair by its left and right values for a specific test question.
     *
     * @param questionId the ID of the test question
     * @param leftValue the left value of the pair
     * @param rightValue the right value of the pair
     * @return the matching pair if found
     */
    TestMatchingPair findByTestQuestionIdAndLeftValueAndRightValue(Long questionId, String leftValue, String rightValue);
    
    /**
     * Find all matching pairs with a specific left value for a test question.
     *
     * @param questionId the ID of the test question
     * @param leftValue the left value to search for
     * @return list of matching pairs with the specified left value
     */
    List<TestMatchingPair> findByTestQuestionIdAndLeftValue(Long questionId, String leftValue);
    
    /**
     * Find all matching pairs with a specific right value for a test question.
     *
     * @param questionId the ID of the test question
     * @param rightValue the right value to search for
     * @return list of matching pairs with the specified right value
     */
    List<TestMatchingPair> findByTestQuestionIdAndRightValue(Long questionId, String rightValue);
    
    /**
     * Count the number of matching pairs for a specific test question.
     *
     * @param questionId the ID of the test question
     * @return the count of matching pairs
     */
    long countByTestQuestionId(Long questionId);
    
    /**
     * Delete all matching pairs for a specific test question.
     *
     * @param questionId the ID of the test question
     */
    void deleteByTestQuestionId(Long questionId);
    
    /**
     * Check if a matching pair exists for a specific test question.
     *
     * @param questionId the ID of the test question
     * @param leftValue the left value of the pair
     * @param rightValue the right value of the pair
     * @return true if the matching pair exists, false otherwise
     */
    boolean existsByTestQuestionIdAndLeftValueAndRightValue(Long questionId, String leftValue, String rightValue);
}
