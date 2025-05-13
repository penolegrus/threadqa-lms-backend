package com.threadqa.lms.controller.communication;

import com.threadqa.lms.dto.comment.CommentRequest;
import com.threadqa.lms.dto.comment.CommentResponse;
import com.threadqa.lms.service.communication.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CommentResponse comment = commentService.createComment(request, userId);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/entity")
    public ResponseEntity<Page<CommentResponse>> getCommentsByEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getRepliesByParentId(@PathVariable Long commentId) {
        List<CommentResponse> replies = commentService.getRepliesByParentId(commentId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long commentId) {
        CommentResponse comment = commentService.getComment(commentId);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CommentResponse comment = commentService.updateComment(commentId, request, userId);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countCommentsByEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        Integer count = commentService.countCommentsByEntity(entityType, entityId);
        return ResponseEntity.ok(count);
    }
}
