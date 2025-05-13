package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.Test;
import com.threadqa.lms.model.course.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    List<Test> findByTopic(Topic topic);

    Page<Test> findByTopic(Topic topic, Pageable pageable);

    Page<Test> findByTopicId(Long topicId, Pageable pageable);

    List<Test> findByTopicIdAndIsPublishedTrue(Long topicId);

    @Query("SELECT t FROM Test t JOIN t.topic tp JOIN tp.course c WHERE c.id = :courseId")
    Page<Test> findByCourseId(Long courseId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Test t WHERE t.topic.id = :topicId")
    Long countByTopicId(Long topicId);

    @Query("SELECT COUNT(t) FROM Test t JOIN t.topic tp JOIN tp.course c WHERE c.id = :courseId")
    Long countByCourseId(Long courseId);
}