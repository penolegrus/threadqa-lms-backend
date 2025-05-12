package com.lms.repository;

import com.lms.model.Test;
import com.lms.model.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTest(Test test);

    List<TestQuestion> findByTestOrderByOrderIndexAsc(Test test);
}