package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Badge;
import com.threadqa.lms.model.gamification.UserBadge;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUser(User user);

    Page<UserBadge> findByUser(User user, Pageable pageable);

    List<UserBadge> findByUserAndIsDisplayedTrue(User user);

    Optional<UserBadge> findByUserAndBadge(User user, Badge badge);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId")
    Long countBadgesByUser(Long userId);

    @Query("SELECT ub.badge.badgeType, COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId GROUP BY ub.badge.badgeType")
    List<Object[]> countBadgesByUserGroupByType(Long userId);

    @Query("SELECT u.id, COUNT(ub) FROM UserBadge ub JOIN ub.user u GROUP BY u.id ORDER BY COUNT(ub) DESC")
    List<Object[]> getUsersWithBadgeCounts();

    @Query("SELECT ub FROM UserBadge ub WHERE ub.notificationSent = false")
    List<UserBadge> findByNotificationSentFalse();
}
