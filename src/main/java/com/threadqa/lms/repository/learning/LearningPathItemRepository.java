package com.threadqa.lms.repository.learning;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.learning.LearningPath;
import com.threadqa.lms.model.learning.LearningPathItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, Long> {
    
    List<LearningPathItem> findByLearningPathOrderByPosition(LearningPath learningPath);
    
    List<LearningPathItem> findByCourse(Course course);
    
    @Modifying
    @Query("UPDATE LearningPathItem lpi SET lpi.position = lpi.position + 1 " +
           "WHERE lpi.learningPath.id = :pathId AND lpi.position >= :position")
    void incrementPositionsFromIndex(@Param("pathId") Long pathId, @Param("position") Integer position);
    
    @Modifying
    @Query("UPDATE LearningPathItem lpi SET lpi.position = lpi.position - 1 " +
           "WHERE lpi.learningPath.id = :pathId AND lpi.position > :position")
    void decrementPositionsAfterIndex(@Param("pathId") Long pathId, @Param("position") Integer position);
    
    @Query("SELECT MAX(lpi.position) FROM LearningPathItem lpi WHERE lpi.learningPath.id = :pathId")
    Integer findMaxPositionInPath(@Param("pathId") Long pathId);
    
    void deleteByLearningPathAndCourse(LearningPath learningPath, Course course);
}
