package com.threadqa.lms.repository.assessment;

import com.threadqa.lms.model.assessment.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q WHERE q.topic.id = :topicId AND q.isActive = true")
    List<Quiz> findActiveQuizzesByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT q FROM Quiz q WHERE q.topic.course.id = :courseId AND q.isActive = true")
    List<Quiz> findActiveQuizzesByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT q FROM Quiz q WHERE q.createdBy.id = :userId")
    Page<Quiz> findByCreatedById(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.topic.id = :topicId")
    List<Quiz> findByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT q FROM Quiz q WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Quiz> searchQuizzes(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.id = :quizId AND q.isActive = true")
    Optional<Quiz> findActiveQuizById(@Param("quizId") Long quizId);
}
