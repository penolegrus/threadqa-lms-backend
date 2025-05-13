package com.threadqa.lms.controller.stream;

import com.threadqa.lms.dto.stream.*;
import com.threadqa.lms.service.stream.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/streams")
@Tag(name = "Stream Management", description = "APIs for managing live streams")
public class StreamController {
    
    private final StreamService streamService;
    
    @Autowired
    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Create a new stream", description = "Create a new live stream session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Stream created successfully",
                    content = @Content(schema = @Schema(implementation = StreamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<StreamResponse> createStream(
            @Valid @RequestBody StreamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long instructorId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.createStream(request, instructorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get stream by ID", description = "Retrieve a stream by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stream found",
                    content = @Content(schema = @Schema(implementation = StreamResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stream not found")
    })
    public ResponseEntity<StreamResponse> getStream(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id) {
        
        StreamResponse response = streamService.getStream(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all streams", description = "Retrieve all streams with pagination")
    public ResponseEntity<Page<StreamResponse>> getAllStreams(Pageable pageable) {
        Page<StreamResponse> streams = streamService.getAllStreams(pageable);
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get streams by course", description = "Retrieve all streams for a specific course")
    public ResponseEntity<Page<StreamResponse>> getStreamsByCourse(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            Pageable pageable) {
        
        Page<StreamResponse> streams = streamService.getStreamsByCourse(courseId, pageable);
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get streams by instructor", description = "Retrieve all streams by a specific instructor")
    public ResponseEntity<Page<StreamResponse>> getStreamsByInstructor(
            @Parameter(description = "Instructor ID") @PathVariable Long instructorId,
            Pageable pageable) {
        
        Page<StreamResponse> streams = streamService.getStreamsByInstructor(instructorId, pageable);
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get streams by status", description = "Retrieve all streams with a specific status")
    public ResponseEntity<Page<StreamResponse>> getStreamsByStatus(
            @Parameter(description = "Stream status (SCHEDULED, LIVE, COMPLETED, CANCELLED)") 
            @PathVariable String status,
            Pageable pageable) {
        
        Page<StreamResponse> streams = streamService.getStreamsByStatus(status, pageable);
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming streams", description = "Retrieve all upcoming streams")
    public ResponseEntity<List<StreamResponse>> getUpcomingStreams() {
        List<StreamResponse> streams = streamService.getUpcomingStreams();
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/live")
    @Operation(summary = "Get live streams", description = "Retrieve all currently live streams")
    public ResponseEntity<List<StreamResponse>> getLiveStreams() {
        List<StreamResponse> streams = streamService.getLiveStreams();
        return ResponseEntity.ok(streams);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Update stream", description = "Update an existing stream")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stream updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Stream not found")
    })
    public ResponseEntity<StreamResponse> updateStream(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id,
            @Valid @RequestBody StreamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long instructorId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.updateStream(id, request, instructorId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Update stream status", description = "Update the status of an existing stream")
    public ResponseEntity<StreamResponse> updateStreamStatus(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id,
            @Valid @RequestBody StreamStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long instructorId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.updateStreamStatus(id, request, instructorId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/recording")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Add stream recording", description = "Add a recording URL to a completed stream")
    public ResponseEntity<StreamResponse> addStreamRecording(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id,
            @Valid @RequestBody StreamRecordingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long instructorId = Long.parseLong(userDetails.getUsername());
        StreamResponse response = streamService.addStreamRecording(id, request, instructorId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Delete stream", description = "Delete an existing stream")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Stream deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Stream not found")
    })
    public ResponseEntity<Void> deleteStream(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long instructorId = Long.parseLong(userDetails.getUsername());
        streamService.deleteStream(id, instructorId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/join")
    @Operation(summary = "Join stream", description = "Join a live stream as a participant")
    public ResponseEntity<StreamParticipantResponse> joinStream(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamParticipantResponse response = streamService.joinStream(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave stream", description = "Leave a live stream as a participant")
    public ResponseEntity<StreamParticipantResponse> leaveStream(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        StreamParticipantResponse response = streamService.leaveStream(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/participants")
    @Operation(summary = "Get stream participants", description = "Get all participants of a stream")
    public ResponseEntity<List<StreamParticipantResponse>> getStreamParticipants(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id) {
        
        List<StreamParticipantResponse> participants = streamService.getStreamParticipants(id);
        return ResponseEntity.ok(participants);
    }
    
    @GetMapping("/{id}/participants/active")
    @Operation(summary = "Get active stream participants", description = "Get all active participants of a stream")
    public ResponseEntity<List<StreamParticipantResponse>> getActiveStreamParticipants(
            @Parameter(description = "Stream ID") @PathVariable("id") Long id) {
        
        List<StreamParticipantResponse> participants = streamService.getActiveStreamParticipants(id);
        return ResponseEntity.ok(participants);
    }
}
