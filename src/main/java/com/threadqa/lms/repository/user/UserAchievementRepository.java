package com.threadqa.lms.repository.user;

import com.threadqa.lms.model.achievement.Achievement;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.model.user.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUser(User user);

    Page<UserAchievement> findByUser(User user, Pageable pageable);

    List<UserAchievement> findByUserAndIsCompletedTrue(User user);

    Page<UserAchievement> findByUserAndIsCompletedTrue(User user, Pageable pageable);

    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.isCompleted = true")
    Long countCompletedByUserId(Long userId);

    @Query("SELECT SUM(a.xpReward) FROM UserAchievement ua JOIN ua.achievement a WHERE ua.user.id = :userId AND ua.isCompleted = true")
    Integer getTotalXpByUserId(Long userId);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.achievement.id = :achievementId AND ua.isCompleted = true")
    Long countCompletedByAchievementId(Long achievementId);
}
