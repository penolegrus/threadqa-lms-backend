package com.lms.service;

import com.lms.dto.stream.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.StreamMapper;
import com.lms.model.Course;
import com.lms.model.Stream;
import com.lms.model.StreamChatMessage;
import com.lms.model.StreamViewer;
import com.lms.model.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.StreamChatMessageRepository;
import com.lms.repository.StreamRepository;
import com.lms.repository.StreamViewerRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepository;
    private final StreamChatMessageRepository chatMessageRepository;
    private final StreamViewerRepository viewerRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StreamMapper streamMapper;

    @Transactional
    public StreamResponse createStream(StreamRequest request, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Verify instructor is authorized to create stream for this course
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("You are not authorized to create streams for this course");
        }

        Stream stream = Stream.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .course(course)
                .instructor(instructor)
                .streamKey(generateStreamKey())
                .isLive(false)
                .isRecorded(request.getIsRecorded())
                .scheduledStartTime(request.getScheduledStartTime())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Stream savedStream = streamRepository.save(stream);

        return streamMapper.toStreamResponse(savedStream, 0L);
    }

    @Transactional(readOnly = true)
    public StreamResponse getStream(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));

        Long activeViewers = viewerRepository.countActiveViewersByStream(streamId);

        return streamMapper.toStreamResponse(stream, activeViewers);
    }

    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByCourse(Long courseId, Pageable pageable) {
        Page<Stream> streams = streamRepository.findByCourseId(courseId, pageable);

        return streams.map(stream -> {
            Long activeViewers = viewerRepository.countActiveViewersByStream(stream.getId());
            return streamMapper.toStreamResponse(stream, activeViewers);
        });
    }

    @Transactional
    public StreamResponse startStream(Long streamId, Long instructorId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));

        // Verify instructor is authorized
        if (!stream.getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("You are not authorized to start this stream");
        }

        if (stream.getIsLive()) {
            throw new IllegalStateException("Stream is already live");
        }

        stream.setIsLive(true);
        stream.setActualStartTime(ZonedDateTime.now());
        stream.setUpdatedAt(ZonedDateTime.now());

        Stream updatedStream = streamRepository.save(stream);

        return streamMapper.toStreamResponse(updatedStream, 0L);
    }

    @Transactional
    public StreamResponse endStream(Long streamId, Long instructorId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));

        // Verify instructor is authorized
        if (!stream.getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("You are not authorized to end this stream");
        }

        if (!stream.getIsLive()) {
            throw new IllegalStateException("Stream is not live");
        }

        stream.setIsLive(false);
        stream.setEndTime(ZonedDateTime.now());
        stream.setUpdatedAt(ZonedDateTime.now());

        // Close all active viewer sessions
        List<StreamViewer> activeViewers = viewerRepository.findByStreamId(streamId).stream()
                .filter(viewer -> viewer.getLeftAt() == null)
                .collect(Collectors.toList());

        for (StreamViewer viewer : activeViewers) {
            viewer.setLeftAt(ZonedDateTime.now());
            long watchTimeSeconds = (viewer.getLeftAt().toEpochSecond() - viewer.getJoinedAt().toEpochSecond());
            viewer.setTotalWatchTimeSeconds(watchTimeSeconds);
            viewerRepository.save(viewer);
        }

        Stream updatedStream = streamRepository.save(stream);

        return streamMapper.toStreamResponse(updatedStream, 0L);
    }

    @Transactional
    public void joinStream(Long streamId, Long userId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!stream.getIsLive()) {
            throw new IllegalStateException("Stream is not live");
        }

        // Check if user is already viewing
        viewerRepository.findActiveViewerByStreamAndUser(streamId, userId)
                .ifPresent(viewer -> {
                    throw new IllegalStateException("User is already viewing this stream");
                });

        StreamViewer viewer = StreamViewer.builder()
                .stream(stream)
                .user(user)
                .joinedAt(ZonedDateTime.now())
                .build();

        viewerRepository.save(viewer);
    }

    @Transactional
    public void leaveStream(Long streamId, Long userId) {
        StreamViewer viewer = viewerRepository.findActiveViewerByStreamAndUser(streamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active viewer not found"));

        viewer.setLeftAt(ZonedDateTime.now());
        long watchTimeSeconds = (viewer.getLeftAt().toEpochSecond() - viewer.getJoinedAt().toEpochSecond());
        viewer.setTotalWatchTimeSeconds(watchTimeSeconds);

        viewerRepository.save(viewer);
    }

    @Transactional
    public StreamChatMessageResponse sendChatMessage(StreamChatMessageRequest request, Long userId) {
        Stream stream = streamRepository.findById(request.getStreamId())
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!stream.getIsLive()) {
            throw new IllegalStateException("Cannot send message to a stream that is not live");
        }

        boolean isInstructor = stream.getInstructor().getId().equals(userId);

        StreamChatMessage chatMessage = StreamChatMessage.builder()
                .stream(stream)
                .user(user)
                .message(request.getMessage())
                .sentAt(ZonedDateTime.now())
                .isPinned(false)
                .isInstructorMessage(isInstructor)
                .build();

        StreamChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return streamMapper.toChatMessageResponse(savedMessage);
    }

    @Transactional
    public StreamChatMessageResponse pinChatMessage(Long messageId, Long instructorId) {
        StreamChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat message not found"));

        // Verify instructor is authorized
        if (!message.getStream().getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("You are not authorized to pin messages in this stream");
        }

        message.setIsPinned(true);
        StreamChatMessage updatedMessage = chatMessageRepository.save(message);

        return streamMapper.toChatMessageResponse(updatedMessage);
    }

    @Transactional(readOnly = true)
    public Page<StreamChatMessageResponse> getStreamChatMessages(Long streamId, Pageable pageable) {
        Page<StreamChatMessage> messages = chatMessageRepository.findByStreamIdOrderBySentAtAsc(streamId, pageable);

        return messages.map(streamMapper::toChatMessageResponse);
    }

    @Transactional(readOnly = true)
    public Page<StreamChatMessageResponse> getPinnedStreamChatMessages(Long streamId, Pageable pageable) {
        Page<StreamChatMessage> messages = chatMessageRepository.findByStreamIdAndIsPinnedTrueOrderBySentAtAsc(streamId, pageable);

        return messages.map(streamMapper::toChatMessageResponse);
    }

    @Transactional(readOnly = true)
    public List<StreamResponse> getLiveStreams() {
        List<Stream> liveStreams = streamRepository.findAllLiveStreams();

        return liveStreams.stream()
                .map(stream -> {
                    Long activeViewers = viewerRepository.countActiveViewersByStream(stream.getId());
                    return streamMapper.toStreamResponse(stream, activeViewers);
                })
                .collect(Collectors.toList());
    }

    private String generateStreamKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}