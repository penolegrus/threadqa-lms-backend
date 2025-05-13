package com.threadqa.lms.service.stream;

import com.threadqa.lms.dto.stream.*;
import com.threadqa.lms.exception.AccessDeniedException;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.StreamMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.stream.Stream;
import com.threadqa.lms.model.stream.StreamParticipant;
import com.threadqa.lms.model.stream.StreamStatus;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.stream.StreamParticipantRepository;
import com.threadqa.lms.repository.stream.StreamRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StreamService {
    
    private final StreamRepository streamRepository;
    private final StreamParticipantRepository participantRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StreamMapper streamMapper;
    private final NotificationService notificationService;
    
    @Autowired
    public StreamService(
            StreamRepository streamRepository,
            StreamParticipantRepository participantRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            StreamMapper streamMapper,
            NotificationService notificationService) {
        this.streamRepository = streamRepository;
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.streamMapper = streamMapper;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public StreamResponse createStream(StreamRequest request, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        
        Stream stream = streamMapper.toEntity(request);
        stream.setInstructor(instructor);
        
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            stream.setCourse(course);
        }
        
        Stream savedStream = streamRepository.save(stream);
        
        // Notify course participants if the stream is associated with a course
        if (request.getCourseId() != null) {
            notifyStreamCreated(savedStream);
        }
        
        return streamMapper.toResponse(savedStream);
    }
    
    @Transactional(readOnly = true)
    public StreamResponse getStream(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));
        
        return streamMapper.toResponse(stream);
    }
    
    @Transactional(readOnly = true)
    public Page<StreamResponse> getAllStreams(Pageable pageable) {
        Page<Stream> streams = streamRepository.findAll(pageable);
        return streams.map(streamMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByCourse(Long courseId, Pageable pageable) {
        Page<Stream> streams = streamRepository.findByCourseId(courseId, pageable);
        return streams.map(streamMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByInstructor(Long instructorId, Pageable pageable) {
        Page<Stream> streams = streamRepository.findByInstructorId(instructorId, pageable);
        return streams.map(streamMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByStatus(String status, Pageable pageable) {
        try {
            StreamStatus streamStatus = StreamStatus.valueOf(status.toUpperCase());
            Page<Stream> streams = streamRepository.findByStatus(streamStatus, pageable);
            return streams.map(streamMapper::toResponse);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid stream status: " + status);
        }
    }
    
    @Transactional(readOnly = true)
    public List<StreamResponse> getUpcomingStreams() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(7); // Get streams scheduled in the next 7 days
        List<Stream> streams = streamRepository.findUpcomingStreams(threshold);
        return streamMapper.toResponseList(streams);
    }
    
    @Transactional(readOnly = true)
    public List<StreamResponse> getLiveStreams() {
        List<Stream> streams = streamRepository.findLiveStreams();
        return streamMapper.toResponseList(streams);
    }
    
    @Transactional
    public StreamResponse updateStream(Long streamId, StreamRequest request, Long instructorId) {
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this stream"));
        
        stream.setTitle(request.getTitle());
        stream.setDescription(request.getDescription());
        stream.setStreamUrl(request.getStreamUrl());
        stream.setScheduledStartTime(request.getScheduledStartTime());
        stream.setIsRecorded(request.getIsRecorded());
        
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            stream.setCourse(course);
        } else {
            stream.setCourse(null);
        }
        
        Stream updatedStream = streamRepository.save(stream);
        
        // Notify participants about the update
        notifyStreamUpdated(updatedStream);
        
        return streamMapper.toResponse(updatedStream);
    }
    
    @Transactional
    public StreamResponse updateStreamStatus(Long streamId, StreamStatusUpdateRequest request, Long instructorId) {
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this stream"));
        
        try {
            StreamStatus newStatus = StreamStatus.valueOf(request.getStatus().toUpperCase());
            stream.setStatus(newStatus);
            
            // Update timestamps based on status
            if (newStatus == StreamStatus.LIVE && stream.getActualStartTime() == null) {
                stream.setActualStartTime(LocalDateTime.now());
            } else if (newStatus == StreamStatus.COMPLETED && stream.getEndTime() == null) {
                stream.setEndTime(LocalDateTime.now());
            }
            
            Stream updatedStream = streamRepository.save(stream);
            
            // Notify participants about the status change
            notifyStreamStatusChanged(updatedStream);
            
            return streamMapper.toResponse(updatedStream);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid stream status: " + request.getStatus());
        }
    }
    
    @Transactional
    public StreamResponse addStreamRecording(Long streamId, StreamRecordingRequest request, Long instructorId) {
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this stream"));
        
        if (!stream.isRecorded()) {
            throw new BadRequestException("This stream is not marked as recorded");
        }
        
        stream.setRecordingUrl(request.getRecordingUrl());
        Stream updatedStream = streamRepository.save(stream);
        
        // Notify participants about the recording
        notifyStreamRecordingAdded(updatedStream);
        
        return streamMapper.toResponse(updatedStream);
    }
    
    @Transactional
    public void deleteStream(Long streamId, Long instructorId) {
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete this stream"));
        
        // Delete all participants first
        List<StreamParticipant> participants = participantRepository.findByStreamId(streamId);
        participantRepository.deleteAll(participants);
        
        // Delete the stream
        streamRepository.delete(stream);
        
        // Notify participants about the cancellation
        notifyStreamCancelled(stream);
    }
    
    @Transactional
    public StreamParticipantResponse joinStream(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (stream.getStatus() != StreamStatus.LIVE) {
            throw new BadRequestException("Stream is not live");
        }
        
        // Check if user is already a participant
        Optional<StreamParticipant> existingParticipant = participantRepository.findByStreamIdAndUserId(streamId, userId);
        
        if (existingParticipant.isPresent()) {
            StreamParticipant participant = existingParticipant.get();
            
            // If participant left before, update their status
            if (!participant.isActive()) {
                participant.setActive(true);
                participant.setJoinedAt(LocalDateTime.now());
                participant.setLeftAt(null);
                participantRepository.save(participant);
            }
            
            return streamMapper.toParticipantResponse(participant);
        } else {
            // Create new participant
            StreamParticipant participant = StreamParticipant.builder()
                    .stream(stream)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            
            StreamParticipant savedParticipant = participantRepository.save(participant);
            return streamMapper.toParticipantResponse(savedParticipant);
        }
    }
    
    @Transactional
    public StreamParticipantResponse leaveStream(Long streamId, Long userId) {
        StreamParticipant participant = participantRepository.findByStreamIdAndUserId(streamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        
        participant.setActive(false);
        participant.setLeftAt(LocalDateTime.now());
        
        StreamParticipant updatedParticipant = participantRepository.save(participant);
        return streamMapper.toParticipantResponse(updatedParticipant);
    }
    
    @Transactional(readOnly = true)
    public List<StreamParticipantResponse> getStreamParticipants(Long streamId) {
        List<StreamParticipant> participants = participantRepository.findByStreamId(streamId);
        return streamMapper.toParticipantResponseList(participants);
    }
    
    @Transactional(readOnly = true)
    public List<StreamParticipantResponse> getActiveStreamParticipants(Long streamId) {
        List<StreamParticipant> participants = participantRepository.findActiveParticipantsByStreamId(streamId);
        return streamMapper.toParticipantResponseList(participants);
    }
    
    // Helper methods for notifications
    private void notifyStreamCreated(Stream stream) {
        if (stream.getCourse() != null) {
            // Notify all enrolled students in the course
            String message = "New live stream scheduled: " + stream.getTitle() + 
                    " on " + stream.getScheduledStartTime();
            
            // Implementation depends on NotificationService
            // notificationService.notifyCourseParticipants(stream.getCourse().getId(), message);
        }
    }
    
    private void notifyStreamUpdated(Stream stream) {
        if (stream.getCourse() != null) {
            String message = "Stream updated: " + stream.getTitle() + 
                    " on " + stream.getScheduledStartTime();
            
            // Implementation depends on NotificationService
            // notificationService.notifyCourseParticipants(stream.getCourse().getId(), message);
        }
    }
    
    private void notifyStreamStatusChanged(Stream stream) {
        if (stream.getStatus() == StreamStatus.LIVE) {
            String message = "Stream is now LIVE: " + stream.getTitle();
            
            // Implementation depends on NotificationService
            // notificationService.notifyCourseParticipants(stream.getCourse().getId(), message);
        } else if (stream.getStatus() == StreamStatus.COMPLETED) {
            String message = "Stream has ended: " + stream.getTitle();
            
            // Implementation depends on NotificationService
            // notificationService.notifyCourseParticipants(stream.getCourse().getId(), message);
        }
    }
    
    private void notifyStreamRecordingAdded(Stream stream) {
        String message = "Recording available for: " + stream.getTitle();
        
        // Implementation depends on NotificationService
        // notificationService.notifyCourseParticipants(stream.getCourse().getId(), message);
    }
    
    private void notifyStreamCancelled(Stream stream) {
        String message = "Stream cancelled: " + stream.getTitle();
        
        // Implementation depends on NotificationService
        // notificationService.notifyCourseParticipants(stream.getCourse().getId(), message);
    }
}
