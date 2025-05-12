package com.lms.repository;

import com.lms.model.Course;
import com.lms.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    Page<Topic> findByCourse(Course course, Pageable pageable);

    Page<Topic> findByCourseId(Long courseId, Pageable pageable);

    List<Topic> findByCourseOrderByOrderIndexAsc(Course course);

    @Query("SELECT MAX(t.orderIndex) FROM Topic t WHERE t.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(Long courseId);
}