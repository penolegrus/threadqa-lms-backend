package com.lms.controller;

import com.lms.dto.stream.StreamChatMessageRequest;
import com.lms.dto.stream.StreamChatMessageResponse;
import com.lms.dto.stream.StreamRequest;
import com.lms.dto.stream.StreamResponse;
import com.lms.service.StreamService;
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
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @PostMapping
    public ResponseEntity<StreamResponse> createStream(
            @Valid @RequestBody StreamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.createStream(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{streamId}")
    public ResponseEntity<StreamResponse> getStream(@PathVariable Long streamId) {
        StreamResponse response = streamService.getStream(streamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<StreamResponse>> getStreamsByCourse(
            @PathVariable Long courseId,
            Pageable pageable) {
        Page<StreamResponse> streams = streamService.getStreamsByCourse(courseId, pageable);
        return ResponseEntity.ok(streams);
    }

    @PostMapping("/{streamId}/start")
    public ResponseEntity<StreamResponse> startStream(
            @PathVariable Long streamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.startStream(streamId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{streamId}/end")
    public ResponseEntity<StreamResponse> endStream(
            @PathVariable Long streamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.endStream(streamId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{streamId}/join")
    public ResponseEntity<Void> joinStream(
            @PathVariable Long streamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        streamService.joinStream(streamId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{streamId}/leave")
    public ResponseEntity<Void> leaveStream(
            @PathVariable Long streamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        streamService.leaveStream(streamId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/chat")
    public ResponseEntity<StreamChatMessageResponse> sendChatMessage(
            @Valid @RequestBody StreamChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamChatMessageResponse response = streamService.sendChatMessage(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/{messageId}/pin")
    public ResponseEntity<StreamChatMessageResponse> pinChatMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamChatMessageResponse response = streamService.pinChatMessage(messageId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{streamId}/chat")
    public ResponseEntity<Page<StreamChatMessageResponse>> getStreamChatMessages(
            @PathVariable Long streamId,
            Pageable pageable) {
        Page<StreamChatMessageResponse> messages = streamService.getStreamChatMessages(streamId, pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{streamId}/chat/pinned")
    public ResponseEntity<Page<StreamChatMessageResponse>> getPinnedStreamChatMessages(
            @PathVariable Long streamId,
            Pageable pageable) {
        Page<StreamChatMessageResponse> messages = streamService.getPinnedStreamChatMessages(streamId, pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/live")
    public ResponseEntity<List<StreamResponse>> getLiveStreams() {
        List<StreamResponse> liveStreams = streamService.getLiveStreams();
        return ResponseEntity.ok(liveStreams);
    }
}