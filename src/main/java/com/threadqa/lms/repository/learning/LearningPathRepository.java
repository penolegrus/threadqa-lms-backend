package com.threadqa.lms.repository.learning;

import com.threadqa.lms.model.learning.LearningPath;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    
    Optional<LearningPath> findBySlug(String slug);
    
    Page<LearningPath> findByPublishedTrue(Pageable pageable);
    
    Page<LearningPath> findByPublishedTrueAndFeaturedTrue(Pageable pageable);
    
    Page<LearningPath> findByCreatedBy(User createdBy, Pageable pageable);
    
    @Query("SELECT lp FROM LearningPath lp WHERE lp.published = true AND " +
           "(LOWER(lp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(lp.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LearningPath> searchPublishedLearningPaths(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT lp FROM LearningPath lp JOIN lp.items lpi WHERE lpi.course.id = :courseId")
    List<LearningPath> findByContainsCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(lp) FROM LearningPath lp WHERE lp.difficultyLevel = :level AND lp.published = true")
    Long countByDifficultyLevelAndPublished(@Param("level") LearningPath.DifficultyLevel level);
}
