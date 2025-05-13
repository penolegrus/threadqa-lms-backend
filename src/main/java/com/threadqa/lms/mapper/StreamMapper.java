package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.stream.*;
import com.threadqa.lms.model.stream.Stream;
import com.threadqa.lms.model.stream.StreamParticipant;
import com.threadqa.lms.model.stream.StreamStatus;
import com.threadqa.lms.repository.stream.StreamParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StreamMapper {
    
    private final UserMapper userMapper;
    private final StreamParticipantRepository participantRepository;
    
    @Autowired
    public StreamMapper(UserMapper userMapper, StreamParticipantRepository participantRepository) {
        this.userMapper = userMapper;
        this.participantRepository = participantRepository;
    }
    
    public Stream toEntity(StreamRequest request) {
        return Stream.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .streamUrl(request.getStreamUrl())
                .scheduledStartTime(request.getScheduledStartTime())
                .status(StreamStatus.SCHEDULED)
                .isRecorded(request.getIsRecorded())
                .build();
    }
    
    public StreamResponse toResponse(Stream stream) {
        Long activeParticipants = participantRepository.countActiveParticipantsByStreamId(stream.getId());
        
        return StreamResponse.builder()
                .id(stream.getId())
                .title(stream.getTitle())
                .description(stream.getDescription())
                .streamUrl(stream.getStreamUrl())
                .scheduledStartTime(stream.getScheduledStartTime())
                .actualStartTime(stream.getActualStartTime())
                .endTime(stream.getEndTime())
                .status(stream.getStatus().name())
                .isRecorded(stream.isRecorded())
                .recordingUrl(stream.getRecordingUrl())
                .courseId(stream.getCourse() != null ? stream.getCourse().getId() : null)
                .courseName(stream.getCourse() != null ? stream.getCourse().getTitle() : null)
                .instructor(stream.getInstructor() != null ? userMapper.toDTO(stream.getInstructor()) : null)
                .activeParticipants(activeParticipants)
                .createdAt(stream.getCreatedAt())
                .updatedAt(stream.getUpdatedAt())
                .build();
    }
    
    public List<StreamResponse> toResponseList(List<Stream> streams) {
        return streams.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public StreamParticipantResponse toParticipantResponse(StreamParticipant participant) {
        return StreamParticipantResponse.builder()
                .id(participant.getId())
                .streamId(participant.getStream().getId())
                .user(userMapper.toDTO(participant.getUser()))
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .isActive(participant.isActive())
                .build();
    }
    
    public List<StreamParticipantResponse> toParticipantResponseList(List<StreamParticipant> participants) {
        return participants.stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());
    }
}
