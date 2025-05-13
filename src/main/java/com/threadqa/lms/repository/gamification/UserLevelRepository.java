package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.UserLevel;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    Optional<UserLevel> findByUser(User user);

    @Query("SELECT ul FROM UserLevel ul WHERE ul.user.id = :userId")
    Optional<UserLevel> findByUserId(Long userId);

    @Query("SELECT AVG(ul.level.levelNumber) FROM UserLevel ul")
    Double getAverageUserLevel();

    @Query("SELECT ul.level.levelNumber, COUNT(ul) FROM UserLevel ul GROUP BY ul.level.levelNumber ORDER BY ul.level.levelNumber")
    List<Object[]> getUserCountByLevel();
}
