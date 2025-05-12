package com.lms.repository;

import com.lms.model.Homework;
import com.lms.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    List<Homework> findByTopic(Topic topic);

    List<Homework> findByTopicId(Long topicId);
}