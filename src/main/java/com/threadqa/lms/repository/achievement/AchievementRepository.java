package com.threadqa.lms.repository.achievement;

import com.threadqa.lms.model.achievement.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByName(String name);

    List<Achievement> findByType(Achievement.AchievementType type);

    boolean existsByName(String name);
}
