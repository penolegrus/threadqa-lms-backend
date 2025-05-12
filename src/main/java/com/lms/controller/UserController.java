package com.lms.controller;

import com.lms.dto.user.PasswordChangeRequest;
import com.lms.dto.user.UserProfileDTO;
import com.lms.dto.user.UserUpdateRequest;
import com.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProfileDTO profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        UserProfileDTO profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProfileDTO profile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<UserProfileDTO> uploadUserAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProfileDTO profile = userService.uploadUserAvatar(userId, file);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/instructors")
    public ResponseEntity<Page<UserProfileDTO>> getInstructors(Pageable pageable) {
        Page<UserProfileDTO> instructors = userService.getInstructors(pageable);
        return ResponseEntity.ok(instructors);
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<UserProfileDTO>> getStudents(Pageable pageable) {
        Page<UserProfileDTO> students = userService.getStudents(pageable);
        return ResponseEntity.ok(students);
    }

    @PostMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> assignRole(
            @PathVariable Long userId,
            @PathVariable String role) {
        UserProfileDTO profile = userService.assignRole(userId, role);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> removeRole(
            @PathVariable Long userId,
            @PathVariable String role) {
        UserProfileDTO profile = userService.removeRole(userId, role);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> disableUser(@PathVariable Long userId) {
        UserProfileDTO profile = userService.disableUser(userId);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> enableUser(@PathVariable Long userId) {
        UserProfileDTO profile = userService.enableUser(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Page<UserProfileDTO>> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        Page<UserProfileDTO> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }
}