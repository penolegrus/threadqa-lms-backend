package com.threadqa.lms.repository.progress;

import com.threadqa.lms.model.progress.UserActivity;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findByUser(User user);

    Page<UserActivity> findByUser(User user, Pageable pageable);

    List<UserActivity> findByUserAndActivityType(User user, UserActivity.ActivityType activityType);

    Page<UserActivity> findByUserAndActivityType(User user, UserActivity.ActivityType activityType, Pageable pageable);

    List<UserActivity> findByUserAndEntityTypeAndEntityId(User user, String entityType, Long entityId);

    Page<UserActivity> findByUserAndEntityTypeAndEntityId(User user, String entityType, Long entityId, Pageable pageable);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityType = :activityType")
    Long countByUserAndActivityType(Long userId, UserActivity.ActivityType activityType);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.user.id = :userId AND ua.createdAt >= :startDate")
    Long countByUserAndCreatedAtAfter(Long userId, ZonedDateTime startDate);

    @Query("SELECT COUNT(DISTINCT ua.user.id) FROM UserActivity ua WHERE ua.activityType = :activityType AND ua.createdAt >= :startDate")
    Long countUniqueUsersByActivityTypeAndCreatedAtAfter(UserActivity.ActivityType activityType, ZonedDateTime startDate);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.entityType = :entityType AND ua.entityId = :entityId")
    Long countByEntityTypeAndEntityId(String entityType, Long entityId);
}
