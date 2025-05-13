package com.threadqa.lms.controller.communication;

import com.threadqa.lms.dto.chat.ChatRequest;
import com.threadqa.lms.dto.chat.ChatResponse;
import com.threadqa.lms.dto.chat.MessageRequest;
import com.threadqa.lms.dto.chat.MessageResponse;
import com.threadqa.lms.service.communication.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> createChat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatResponse chat = chatService.createChat(request, userId);
        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ChatResponse>> getUserChats(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<ChatResponse> chats = chatService.getUserChats(userId, pageable);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatResponse chat = chatService.getChat(chatId, userId);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        MessageResponse message = chatService.sendMessage(chatId, request, userId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            @PathVariable Long chatId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<MessageResponse> messages = chatService.getChatMessages(chatId, userId, pageable);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<ChatResponse> addParticipants(
            @PathVariable Long chatId,
            @RequestBody Set<Long> userIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatResponse chat = chatService.addParticipants(chatId, userIds, userId);
        return ResponseEntity.ok(chat);
    }

    @DeleteMapping("/{chatId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long chatId,
            @PathVariable Long participantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.removeParticipant(chatId, participantId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.deleteChat(chatId, userId);
        return ResponseEntity.noContent().build();
    }
}
