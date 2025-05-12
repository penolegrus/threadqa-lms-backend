package com.lms.controller;

import com.lms.dto.learningpath.*;
import com.lms.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LearningPathResponse> createLearningPath(@Valid @RequestBody LearningPathRequest request) {
        LearningPathResponse response = learningPathService.createLearningPath(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{learningPathId}")
    public ResponseEntity<LearningPathResponse> getLearningPath(@PathVariable Long learningPathId) {
        LearningPathResponse response = learningPathService.getLearningPath(learningPathId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LearningPathResponse>> getAllLearningPaths() {
        List<LearningPathResponse> response = learningPathService.getAllLearningPaths();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        SkillResponse response = learningPathService.createSkill(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/skills/{skillId}")
    public ResponseEntity<SkillResponse> getSkill(@PathVariable Long skillId) {
        SkillResponse response = learningPathService.getSkill(skillId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user-skills")
    public ResponseEntity<UserSkillResponse> updateUserSkill(
            @Valid @RequestBody UserSkillRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserSkillResponse response = learningPathService.updateUserSkill(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-skills")
    public ResponseEntity<List<UserSkillResponse>> getUserSkills(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserSkillResponse> response = learningPathService.getUserSkills(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-skills/{learningPathId}")
    public ResponseEntity<List<UserSkillResponse>> getUserSkillsByLearningPath(
            @PathVariable Long learningPathId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserSkillResponse> response = learningPathService.getUserSkillsByLearningPath(userId, learningPathId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/focus-skills")
    public ResponseEntity<FocusSkillResponse> addFocusSkill(
            @Valid @RequestBody FocusSkillRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        FocusSkillResponse response = learningPathService.addFocusSkill(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/focus-skills/{focusSkillId}")
    public ResponseEntity<Void> removeFocusSkill(
            @PathVariable Long focusSkillId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        learningPathService.removeFocusSkill(focusSkillId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/focus-skills")
    public ResponseEntity<List<FocusSkillResponse>> getUserFocusSkills(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<FocusSkillResponse> response = learningPathService.getUserFocusSkills(userId);
        return ResponseEntity.ok(response);
    }
}