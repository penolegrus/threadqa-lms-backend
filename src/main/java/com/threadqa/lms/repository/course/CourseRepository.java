package com.threadqa.lms.repository.course;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByInstructor(User instructor, Pageable pageable);

    Page<Course> findByIsPublishedTrue(Pageable pageable);

    Page<Course> findByIsFeaturedTrue(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchCourses(String keyword, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.categories cat WHERE cat.id = :categoryId AND c.isPublished = true")
    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
    Long countByInstructorId(Long instructorId);
    
    // Новые методы
    @Query("SELECT COUNT(c) FROM Course c WHERE c.isPublished = true")
    Long countByIsPublishedTrue();
    
    @Query("SELECT c.title FROM Course c WHERE c.id = :courseId")
    String findTitleById(Long courseId);
    
    boolean existsByIdAndInstructorId(Long id, Long instructorId);
}
