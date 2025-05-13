package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.chat.ChatResponse;
import com.threadqa.lms.dto.chat.MessageResponse;
import com.threadqa.lms.model.chat.Chat;
import com.threadqa.lms.model.chat.ChatParticipant;
import com.threadqa.lms.model.chat.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMapper {

    public ChatResponse toChatResponse(Chat chat) {
        if (chat == null) {
            return null;
        }

        List<ChatResponse.ParticipantDTO> participants = chat.getParticipants().stream()
                .map(this::toParticipantDTO)
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getName())
                .type(chat.getType())
                .participants(participants)
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .build();
    }

    public MessageResponse toMessageResponse(Message message) {
        if (message == null) {
            return null;
        }

        return MessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }

    private ChatResponse.ParticipantDTO toParticipantDTO(ChatParticipant participant) {
        return ChatResponse.ParticipantDTO.builder()
                .userId(participant.getUser().getId())
                .userName(participant.getUser().getFirstName() + " " + participant.getUser().getLastName())
                .role(participant.getRole())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}
