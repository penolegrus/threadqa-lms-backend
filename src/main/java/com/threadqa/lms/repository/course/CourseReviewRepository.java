package com.threadqa.lms.repository.course;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseReview;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Page<CourseReview> findByCourse(Course course, Pageable pageable);

    Page<CourseReview> findByCourseId(Long courseId, Pageable pageable);

    Page<CourseReview> findByUser(User user, Pageable pageable);

    Optional<CourseReview> findByUserAndCourse(User user, Course course);

    @Query("SELECT AVG(cr.rating) FROM CourseReview cr WHERE cr.course.id = :courseId")
    Double getAverageRatingByCourseId(Long courseId);

    @Query("SELECT COUNT(cr) FROM CourseReview cr WHERE cr.course.id = :courseId")
    Long countByCourseId(Long courseId);
    
    // Новый метод
    @Query("SELECT AVG(cr.rating) FROM CourseReview cr")
    Double getSystemWideAverageRating();
}
