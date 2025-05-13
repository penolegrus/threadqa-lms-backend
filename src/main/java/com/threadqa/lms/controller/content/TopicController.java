package com.threadqa.lms.controller.content;

import com.threadqa.lms.dto.topic.TopicRequest;
import com.threadqa.lms.dto.topic.TopicResponse;
import com.threadqa.lms.service.content.TopicService;
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

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<Page<TopicResponse>> getAllTopics(Pageable pageable) {
        Page<TopicResponse> topics = topicService.getAllTopics(pageable);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicResponse> getTopic(@PathVariable Long topicId) {
        TopicResponse topic = topicService.getTopic(topicId);
        return ResponseEntity.ok(topic);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<TopicResponse>> getTopicsByCourse(
            @PathVariable Long courseId,
            Pageable pageable) {
        Page<TopicResponse> topics = topicService.getTopicsByCourse(courseId, pageable);
        return ResponseEntity.ok(topics);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TopicResponse> createTopic(
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TopicResponse topic = topicService.createTopic(request, userId);
        return new ResponseEntity<>(topic, HttpStatus.CREATED);
    }

    @PutMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TopicResponse topic = topicService.updateTopic(topicId, request, userId);
        return ResponseEntity.ok(topic);
    }

    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        topicService.deleteTopic(topicId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{topicId}/order")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TopicResponse> updateTopicOrder(
            @PathVariable Long topicId,
            @RequestParam Integer orderIndex,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TopicResponse topic = topicService.updateTopicOrder(topicId, orderIndex, userId);
        return ResponseEntity.ok(topic);
    }
}
