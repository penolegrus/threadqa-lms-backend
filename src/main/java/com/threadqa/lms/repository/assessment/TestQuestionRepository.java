package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.Test;
import com.threadqa.lms.model.assessment.TestQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTest(Test test);

    List<TestQuestion> findByTestOrderByOrderIndexAsc(Test test);

    Page<TestQuestion> findByTest(Test test, Pageable pageable);

    @Query("SELECT COUNT(q) FROM TestQuestion q WHERE q.test.id = :testId")
    Long countByTestId(Long testId);

    @Query("SELECT SUM(q.points) FROM TestQuestion q WHERE q.test.id = :testId")
    Integer getTotalPointsByTestId(Long testId);
}