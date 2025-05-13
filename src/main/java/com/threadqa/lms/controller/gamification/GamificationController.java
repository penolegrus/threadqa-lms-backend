package com.threadqa.lms.controller.gamification;

import com.threadqa.lms.dto.gamification.*;
import com.threadqa.lms.service.gamification.GamificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @PostMapping("/points")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<PointResponse> awardPoints(
            @Valid @RequestBody PointRequest request,
            @RequestParam Long userId) {
        PointResponse response = gamificationService.awardPoints(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/points/total")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> getTotalPoints(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Integer totalPoints = gamificationService.getTotalPoints(userId);
        return ResponseEntity.ok(totalPoints);
    }

    @GetMapping("/points/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Page<PointResponse>> getUserPoints(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<PointResponse> points = gamificationService.getUserPoints(userId, pageable);
        return ResponseEntity.ok(points);
    }

    @PostMapping("/badges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BadgeResponse> createBadge(
            @Valid @RequestBody BadgeRequest request) {
        BadgeResponse response = gamificationService.createBadge(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/badges")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = gamificationService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/badges/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BadgeResponse>> getActiveBadges() {
        List<BadgeResponse> badges = gamificationService.getActiveBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/badges/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Page<UserBadgeResponse>> getUserBadges(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<UserBadgeResponse> badges = gamificationService.getUserBadges(userId, pageable);
        return ResponseEntity.ok(badges);
    }

    @PostMapping("/levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LevelResponse> createLevel(
            @Valid @RequestBody LevelRequest request) {
        LevelResponse response = gamificationService.createLevel(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/levels")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LevelResponse>> getAllLevels() {
        List<LevelResponse> levels = gamificationService.getAllLevels();
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/levels/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<UserLevelResponse> getUserLevel(
            @PathVariable Long userId) {
        UserLevelResponse level = gamificationService.getUserLevel(userId);
        return ResponseEntity.ok(level);
    }

    @PostMapping("/leaderboards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaderboardResponse> createLeaderboard(
            @Valid @RequestBody LeaderboardRequest request) {
        LeaderboardResponse response = gamificationService.createLeaderboard(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardResponse>> getActiveLeaderboards() {
        List<LeaderboardResponse> leaderboards = gamificationService.getActiveLeaderboards();
        return ResponseEntity.ok(leaderboards);
    }

    @GetMapping("/leaderboards/{leaderboardId}/entries")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LeaderboardEntryResponse>> getLeaderboardEntries(
            @PathVariable Long leaderboardId,
            Pageable pageable) {
        Page<LeaderboardEntryResponse> entries = gamificationService.getLeaderboardEntries(leaderboardId, pageable);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/leaderboards/{leaderboardId}/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<LeaderboardEntryResponse> getUserLeaderboardPosition(
            @PathVariable Long leaderboardId,
            @PathVariable Long userId) {
        LeaderboardEntryResponse entry = gamificationService.getUserLeaderboardPosition(leaderboardId, userId);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/streaks/login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreakResponse> updateLoginStreak(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StreakResponse response = gamificationService.updateLoginStreak(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/streaks/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<List<StreakResponse>> getUserStreaks(
            @PathVariable Long userId) {
        List<StreakResponse> streaks = gamificationService.getUserStreaks(userId);
        return ResponseEntity.ok(streaks);
    }

    @GetMapping("/summary/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<GamificationSummaryResponse> getUserGamificationSummary(
            @PathVariable Long userId) {
        GamificationSummaryResponse summary = gamificationService.getUserGamificationSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
