package com.threadqa.lms.repository.learning;

import com.threadqa.lms.model.learning.LearningPath;
import com.threadqa.lms.model.learning.UserLearningPathProgress;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningPathProgressRepository extends JpaRepository<UserLearningPathProgress, Long> {
    
    Optional<UserLearningPathProgress> findByUserAndLearningPath(User user, LearningPath learningPath);
    
    List<UserLearningPathProgress> findByUser(User user);
    
    Page<UserLearningPathProgress> findByUserAndCompletedTrue(User user, Pageable pageable);
    
    Page<UserLearningPathProgress> findByUserAndCompletedFalse(User user, Pageable pageable);
    
    @Query("SELECT COUNT(ulpp) FROM UserLearningPathProgress ulpp WHERE ulpp.learningPath.id = :pathId AND ulpp.completed = true")
    Long countCompletionsByLearningPath(@Param("pathId") Long pathId);
    
    @Query("SELECT COUNT(ulpp) FROM UserLearningPathProgress ulpp WHERE ulpp.learningPath.id = :pathId")
    Long countEnrollmentsByLearningPath(@Param("pathId") Long pathId);
    
    @Query("SELECT AVG(ulpp.progressPercentage) FROM UserLearningPathProgress ulpp WHERE ulpp.learningPath.id = :pathId")
    Double getAverageProgressForLearningPath(@Param("pathId") Long pathId);
    
    @Query("SELECT COUNT(ulpp) FROM UserLearningPathProgress ulpp WHERE ulpp.startedAt >= :startDate")
    Long countNewEnrollmentsSince(@Param("startDate") LocalDateTime startDate);
}
