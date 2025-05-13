package com.threadqa.lms.controller.achievement;

import com.threadqa.lms.dto.achievement.AchievementRequest;
import com.threadqa.lms.dto.achievement.AchievementResponse;
import com.threadqa.lms.dto.achievement.UserAchievementResponse;
import com.threadqa.lms.service.achievement.AchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAllAchievements() {
        List<AchievementResponse> achievements = achievementService.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/{achievementId}")
    public ResponseEntity<AchievementResponse> getAchievement(@PathVariable Long achievementId) {
        AchievementResponse achievement = achievementService.getAchievement(achievementId);
        return ResponseEntity.ok(achievement);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponse> createAchievement(@Valid @RequestBody AchievementRequest request) {
        AchievementResponse achievement = achievementService.createAchievement(request);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED);
    }

    @PutMapping("/{achievementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable Long achievementId,
            @Valid @RequestBody AchievementRequest request) {
        AchievementResponse achievement = achievementService.updateAchievement(achievementId, request);
        return ResponseEntity.ok(achievement);
    }

    @DeleteMapping("/{achievementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAchievement(@PathVariable Long achievementId) {
        achievementService.deleteAchievement(achievementId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<UserAchievementResponse>> getUserAchievements(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<UserAchievementResponse> achievements = achievementService.getUserAchievements(userId, pageable);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<Page<UserAchievementResponse>> getUserCompletedAchievements(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<UserAchievementResponse> achievements = achievementService.getUserCompletedAchievements(userId, pageable);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/user/{userId}/xp")
    public ResponseEntity<Integer> getUserTotalXp(@PathVariable Long userId) {
        Integer totalXp = achievementService.getUserTotalXp(userId);
        return ResponseEntity.ok(totalXp);
    }

    @PostMapping("/user/{userId}/check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> checkAndUpdateAchievements(@PathVariable Long userId) {
        achievementService.checkAndUpdateAchievements(userId);
        return ResponseEntity.noContent().build();
    }
}
