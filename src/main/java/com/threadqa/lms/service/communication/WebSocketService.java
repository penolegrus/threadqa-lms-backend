package com.threadqa.lms.service.communication;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public <T> void sendPrivateNotification(Long userId, T payload) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload
        );
    }

    public <T> void sendToTopic(String topic, T payload) {
        messagingTemplate.convertAndSend("/topic/" + topic, payload);
    }

    public <T> void sendToChat(Long chatId, T payload) {
        messagingTemplate.convertAndSend("/topic/chat." + chatId, payload);
    }

    public <T> void sendToStream(Long streamId, T payload) {
        messagingTemplate.convertAndSend("/topic/stream." + streamId, payload);
    }
}
