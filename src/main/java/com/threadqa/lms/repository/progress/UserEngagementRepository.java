package com.threadqa.lms.repository.progress;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.progress.UserEngagement;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface UserEngagementRepository extends JpaRepository<UserEngagement, Long> {

    List<UserEngagement> findByUser(User user);

    Page<UserEngagement> findByUser(User user, Pageable pageable);

    List<UserEngagement> findByUserAndCourse(User user, Course course);

    Page<UserEngagement> findByUserAndCourse(User user, Course course, Pageable pageable);

    List<UserEngagement> findByUserAndEngagementDateBetween(User user, ZonedDateTime startDate, ZonedDateTime endDate);

    Page<UserEngagement> findByUserAndEngagementDateBetween(User user, ZonedDateTime startDate, ZonedDateTime endDate, Pageable pageable);

    @Query("SELECT SUM(ue.sessionDurationSeconds) FROM UserEngagement ue WHERE ue.user.id = :userId")
    Long getTotalSessionDurationByUser(Long userId);

    @Query("SELECT SUM(ue.sessionDurationSeconds) FROM UserEngagement ue WHERE ue.user.id = :userId AND ue.course.id = :courseId")
    Long getTotalSessionDurationByUserAndCourse(Long userId, Long courseId);

    @Query("SELECT SUM(ue.pageViews) FROM UserEngagement ue WHERE ue.user.id = :userId")
    Long getTotalPageViewsByUser(Long userId);

    @Query("SELECT SUM(ue.interactions) FROM UserEngagement ue WHERE ue.user.id = :userId")
    Long getTotalInteractionsByUser(Long userId);

    @Query("SELECT AVG(ue.sessionDurationSeconds) FROM UserEngagement ue WHERE ue.course.id = :courseId")
    Double getAverageSessionDurationByCourse(Long courseId);

    @Query("SELECT COUNT(DISTINCT ue.user.id) FROM UserEngagement ue WHERE ue.course.id = :courseId AND ue.engagementDate >= :startDate")
    Long countUniqueUsersByCourseAndEngagementDateAfter(Long courseId, ZonedDateTime startDate);
}
