package com.lms.controller;

import com.lms.dto.topic.TopicRequest;
import com.lms.dto.topic.TopicResponse;
import com.lms.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        TopicResponse topic = topicService.createTopic(request);
        return new ResponseEntity<>(topic, HttpStatus.CREATED);
    }

    @PutMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody TopicRequest request) {
        TopicResponse topic = topicService.updateTopic(topicId, request);
        return ResponseEntity.ok(topic);
    }

    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
        topicService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{topicId}/order")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TopicResponse> updateTopicOrder(
            @PathVariable Long topicId,
            @RequestParam Integer orderIndex) {
        TopicResponse topic = topicService.updateTopicOrder(topicId, orderIndex);
        return ResponseEntity.ok(topic);
    }
}