package com.lms.repository;

import com.lms.model.Test;
import com.lms.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    List<Test> findByTopic(Topic topic);

    Page<Test> findByTopicId(Long topicId, Pageable pageable);

    List<Test> findByIsActiveTrue();
}