package com.lms.controller;

import com.lms.dto.chat.ChatMessageRequest;
import com.lms.dto.chat.ChatMessageResponse;
import com.lms.dto.chat.ChatRoomRequest;
import com.lms.dto.chat.ChatRoomResponse;
import com.lms.service.ChatService;
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
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @Valid @RequestBody ChatRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatRoomResponse chatRoom = chatService.createChatRoom(request, userId);
        return new ResponseEntity<>(chatRoom, HttpStatus.CREATED);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<ChatRoomResponse> chatRooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatRoomResponse chatRoom = chatService.getChatRoom(roomId, userId);
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendChatMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatMessageResponse message = chatService.sendChatMessage(roomId, request, userId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<ChatMessageResponse> messages = chatService.getChatMessages(roomId, userId, pageable);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ChatRoomResponse> joinChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ChatRoomResponse chatRoom = chatService.joinChatRoom(roomId, userId);
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.leaveChatRoom(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}/users")
    public ResponseEntity<List<String>> getChatRoomUsers(@PathVariable Long roomId) {
        List<String> users = chatService.getChatRoomUsers(roomId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/rooms/{roomId}/typing")
    public ResponseEntity<Void> sendTypingNotification(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.sendTypingNotification(roomId, userId);
        return ResponseEntity.ok().build();
    }
}