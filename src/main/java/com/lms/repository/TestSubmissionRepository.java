package com.lms.repository;

import com.lms.model.Test;
import com.lms.model.TestSubmission;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestSubmissionRepository extends JpaRepository<TestSubmission, Long> {

    List<TestSubmission> findByUserAndTest(User user, Test test);

    Page<TestSubmission> findByUser(User user, Pageable pageable);

    Page<TestSubmission> findByTest(Test test, Pageable pageable);

    @Query("SELECT ts FROM TestSubmission ts WHERE ts.user.id = :userId AND ts.test.id = :testId ORDER BY ts.score DESC, ts.submittedAt DESC")
    List<TestSubmission> findBestSubmissionByUserAndTest(Long userId, Long testId, Pageable pageable);

    @Query("SELECT AVG(ts.score) FROM TestSubmission ts WHERE ts.test.id = :testId AND ts.submittedAt IS NOT NULL")
    Double getAverageScoreForTest(Long testId);

    @Query("SELECT COUNT(ts) FROM TestSubmission ts WHERE ts.test.id = :testId AND ts.isPassed = true")
    Long countPassedSubmissionsForTest(Long testId);
}