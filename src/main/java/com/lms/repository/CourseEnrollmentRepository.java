package com.lms.repository;

import com.lms.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByUserId(Long userId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    int countByCourseId(Long courseId);
}