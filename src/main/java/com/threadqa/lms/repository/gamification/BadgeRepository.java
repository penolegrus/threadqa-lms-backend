package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByBadgeType(Badge.BadgeType badgeType);

    List<Badge> findByIsActiveTrue();

    List<Badge> findByBadgeTypeAndIsActiveTrue(Badge.BadgeType badgeType);

    List<Badge> findByThresholdLessThanEqual(Integer threshold);

    List<Badge> findByBadgeTypeAndThresholdLessThanEqual(Badge.BadgeType badgeType, Integer threshold);
}
