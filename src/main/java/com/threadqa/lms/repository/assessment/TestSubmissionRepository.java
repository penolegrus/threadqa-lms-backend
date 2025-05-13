package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.Test;
import com.threadqa.lms.model.assessment.TestSubmission;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestSubmissionRepository extends JpaRepository<TestSubmission, Long> {

    List<TestSubmission> findByUser(User user);

    Page<TestSubmission> findByUser(User user, Pageable pageable);

    List<TestSubmission> findByTest(Test test);

    Page<TestSubmission> findByTest(Test test, Pageable pageable);

    List<TestSubmission> findByUserAndTest(User user, Test test);

    Optional<TestSubmission> findTopByUserAndTestOrderByAttemptNumberDesc(User user, Test test);

    @Query("SELECT COUNT(ts) FROM TestSubmission ts WHERE ts.test.id = :testId")
    Long countByTestId(Long testId);

    @Query("SELECT AVG(ts.score) FROM TestSubmission ts WHERE ts.test.id = :testId")
    Double getAverageScoreByTestId(Long testId);

    @Query("SELECT COUNT(ts) FROM TestSubmission ts WHERE ts.test.id = :testId AND ts.isPassed = true")
    Long countPassedByTestId(Long testId);
}