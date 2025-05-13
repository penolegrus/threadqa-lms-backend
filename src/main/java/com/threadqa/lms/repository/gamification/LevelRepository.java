package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    Optional<Level> findByLevelNumber(Integer levelNumber);

    @Query("SELECT l FROM Level l WHERE l.pointsRequired <= :points ORDER BY l.pointsRequired DESC")
    Level findHighestLevelForPoints(Integer points);

    @Query("SELECT l FROM Level l WHERE l.pointsRequired > :points ORDER BY l.pointsRequired ASC")
    Level findNextLevelForPoints(Integer points);
}
