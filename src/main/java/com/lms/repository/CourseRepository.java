package com.lms.repository;

import com.lms.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByTitleContaining(String title, Pageable pageable);

    Page<Course> findByCategory(String category, Pageable pageable);

    Page<Course> findByLevel(Course.CourseLevel level, Pageable pageable);

    Page<Course> findByTitleContainingAndCategory(String title, String category, Pageable pageable);

    Page<Course> findByTitleContainingAndLevel(String title, Course.CourseLevel level, Pageable pageable);

    Page<Course> findByCategoryAndLevel(String category, Course.CourseLevel level, Pageable pageable);

    Page<Course> findByTitleContainingAndCategoryAndLevel(
            String title, String category, Course.CourseLevel level, Pageable pageable);

    @Query("SELECT new map(c.category as id, c.category as name, COUNT(c) as count) " +
            "FROM Course c GROUP BY c.category ORDER BY c.category")
    List<Map<String, Object>> findCourseCategories();
}