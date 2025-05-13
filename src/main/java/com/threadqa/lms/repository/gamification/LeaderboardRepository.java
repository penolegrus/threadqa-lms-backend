package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    List<Leaderboard> findByIsActiveTrue();

    List<Leaderboard> findByLeaderboardTypeAndIsActiveTrue(Leaderboard.LeaderboardType leaderboardType);

    List<Leaderboard> findByTimePeriodAndIsActiveTrue(Leaderboard.TimePeriod timePeriod);

    Optional<Leaderboard> findByLeaderboardTypeAndTimePeriodAndIsActiveTrue(
            Leaderboard.LeaderboardType leaderboardType, Leaderboard.TimePeriod timePeriod);
}
