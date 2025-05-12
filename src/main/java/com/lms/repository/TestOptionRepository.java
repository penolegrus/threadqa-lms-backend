package com.lms.repository;

import com.lms.model.TestOption;
import com.lms.model.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestOptionRepository extends JpaRepository<TestOption, Long> {

    List<TestOption> findByTestQuestion(TestQuestion testQuestion);

    List<TestOption> findByTestQuestionOrderByOrderIndexAsc(TestQuestion testQuestion);

    List<TestOption> findByTestQuestionAndIsCorrectTrue(TestQuestion testQuestion);
}