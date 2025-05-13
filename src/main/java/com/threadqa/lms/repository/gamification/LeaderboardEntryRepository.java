package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Leaderboard;
import com.threadqa.lms.model.gamification.LeaderboardEntry;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    List<LeaderboardEntry> findByLeaderboard(Leaderboard leaderboard);

    Page<LeaderboardEntry> findByLeaderboardOrderByRankAsc(Leaderboard leaderboard, Pageable pageable);

    Optional<LeaderboardEntry> findByLeaderboardAndUser(Leaderboard leaderboard, User user);

    @Query("SELECT le FROM LeaderboardEntry le WHERE le.leaderboard.id = :leaderboardId AND le.rank <= :topRank ORDER BY le.rank ASC")
    List<LeaderboardEntry> findTopRanksByLeaderboard(Long leaderboardId, Integer topRank);

    @Query("SELECT le FROM LeaderboardEntry le WHERE le.leaderboard.id = :leaderboardId AND le.user.id = :userId")
    Optional<LeaderboardEntry> findByLeaderboardIdAndUserId(Long leaderboardId, Long userId);

    @Query("SELECT le FROM LeaderboardEntry le WHERE le.periodEnd < :date")
    List<LeaderboardEntry> findExpiredEntries(ZonedDateTime date);
}
