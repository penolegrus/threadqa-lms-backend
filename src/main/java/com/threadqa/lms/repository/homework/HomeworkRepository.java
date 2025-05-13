package com.threadqa.lms.repository.homework;

import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.homework.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    Page<Homework> findByTopic(Topic topic, Pageable pageable);

    Page<Homework> findByTopicId(Long topicId, Pageable pageable);

    List<Homework> findByTopicIdAndIsPublishedTrue(Long topicId);

    @Query("SELECT h FROM Homework h JOIN h.topic t JOIN t.course c WHERE c.id = :courseId")
    Page<Homework> findByCourseId(Long courseId, Pageable pageable);

    @Query("SELECT COUNT(h) FROM Homework h WHERE h.topic.id = :topicId")
    Long countByTopicId(Long topicId);

    @Query("SELECT COUNT(h) FROM Homework h JOIN h.topic t JOIN t.course c WHERE c.id = :courseId")
    Long countByCourseId(Long courseId);
}
