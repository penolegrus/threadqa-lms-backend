package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Streak;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByUserAndStreakType(User user, Streak.StreakType streakType);

    List<Streak> findByUser(User user);

    @Query("SELECT s FROM Streak s WHERE s.lastActivityDate < :cutoffDate")
    List<Streak> findStreaksToReset(ZonedDateTime cutoffDate);

    @Query("SELECT s FROM Streak s WHERE s.currentStreak >= :minStreak ORDER BY s.currentStreak DESC")
    List<Streak> findTopStreaks(Integer minStreak);

    @Query("SELECT AVG(s.currentStreak) FROM Streak s WHERE s.streakType = :streakType")
    Double getAverageStreakByType(Streak.StreakType streakType);

    @Query("SELECT MAX(s.longestStreak) FROM Streak s WHERE s.streakType = :streakType")
    Integer getMaxLongestStreakByType(Streak.StreakType streakType);
}
