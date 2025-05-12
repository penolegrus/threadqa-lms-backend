package com.lms.mapper;

import com.lms.dto.stream.StreamChatMessageResponse;
import com.lms.dto.stream.StreamResponse;
import com.lms.model.Stream;
import com.lms.model.StreamChatMessage;
import org.springframework.stereotype.Component;

@Component
public class StreamMapper {

    public StreamResponse toStreamResponse(Stream stream, Long activeViewers) {
        if (stream == null) {
            return null;
        }

        return StreamResponse.builder()
                .id(stream.getId())
                .title(stream.getTitle())
                .description(stream.getDescription())
                .courseId(stream.getCourse().getId())
                .courseName(stream.getCourse().getTitle())
                .instructorId(stream.getInstructor().getId())
                .instructorName(stream.getInstructor().getFirstName() + " " + stream.getInstructor().getLastName())
                .streamKey(stream.getStreamKey())
                .streamUrl(stream.getStreamUrl())
                .scheduledStartTime(stream.getScheduledStartTime())
                .actualStartTime(stream.getActualStartTime())
                .endTime(stream.getEndTime())
                .isLive(stream.getIsLive())
                .isRecorded(stream.getIsRecorded())
                .recordingUrl(stream.getRecordingUrl())
                .activeViewers(activeViewers)
                .createdAt(stream.getCreatedAt())
                .updatedAt(stream.getUpdatedAt())
                .build();
    }

    public StreamChatMessageResponse toChatMessageResponse(StreamChatMessage message) {
        if (message == null) {
            return null;
        }

        return StreamChatMessageResponse.builder()
                .id(message.getId())
                .streamId(message.getStream().getId())
                .userId(message.getUser().getId())
                .userName(message.getUser().getFirstName() + " " + message.getUser().getLastName())
                .userAvatar(message.getUser().getAvatarUrl())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .isPinned(message.getIsPinned())
                .isInstructorMessage(message.getIsInstructorMessage())
                .build();
    }
}