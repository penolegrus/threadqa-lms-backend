package com.threadqa.lms.controller.communication;

import com.threadqa.lms.service.communication.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage/{chatId}")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        Long userId = Long.parseLong(principal.getName());
        
        // Создание запроса сообщения
        com.threadqa.lms.dto.chat.MessageRequest request = new com.threadqa.lms.dto.chat.MessageRequest();
        request.setContent(message);
        
        // Отправка сообщения через сервис
        chatService.sendMessage(chatId, request, userId);
    }
}
