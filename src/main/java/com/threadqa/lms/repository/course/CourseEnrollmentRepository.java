package com.threadqa.lms.repository.course;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByUser(User user);

    Page<CourseEnrollment> findByUser(User user, Pageable pageable);

    List<CourseEnrollment> findByCourse(Course course);

    Page<CourseEnrollment> findByCourse(Course course, Pageable pageable);

    Optional<CourseEnrollment> findByUserAndCourse(User user, Course course);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    Long countByCourseId(Long courseId);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.user.id = :userId")
    Long countByUserId(Long userId);

    @Query("SELECT AVG(ce.progress) FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    Double getAverageProgressByCourseId(Long courseId);
}