package com.threadqa.lms.service.communication;

import com.threadqa.lms.dto.chat.ChatRequest;
import com.threadqa.lms.dto.chat.ChatResponse;
import com.threadqa.lms.dto.chat.MessageRequest;
import com.threadqa.lms.dto.chat.MessageResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.ChatMapper;
import com.threadqa.lms.model.chat.Chat;
import com.threadqa.lms.model.chat.ChatParticipant;
import com.threadqa.lms.model.chat.Message;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.chat.ChatParticipantRepository;
import com.threadqa.lms.repository.chat.ChatRepository;
import com.threadqa.lms.repository.chat.MessageRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final WebSocketService webSocketService;

    @Transactional
    public ChatResponse createChat(ChatRequest request, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Создание чата
        Chat chat = Chat.builder()
                .name(request.getName())
                .type(request.getType())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Chat savedChat = chatRepository.save(chat);

        // Добавление создателя как участника
        ChatParticipant creatorParticipant = ChatParticipant.builder()
                .chat(savedChat)
                .user(currentUser)
                .role("ADMIN")
                .joinedAt(ZonedDateTime.now())
                .build();

        participantRepository.save(creatorParticipant);

        // Добавление других участников
        List<ChatParticipant> participants = new ArrayList<>();
        participants.add(creatorParticipant);

        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            for (Long userId : request.getParticipantIds()) {
                if (!userId.equals(currentUserId)) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

                    ChatParticipant participant = ChatParticipant.builder()
                            .chat(savedChat)
                            .user(user)
                            .role("MEMBER")
                            .joinedAt(ZonedDateTime.now())
                            .build();

                    participants.add(participantRepository.save(participant));
                }
            }
        }

        savedChat.setParticipants(participants);

        return chatMapper.toChatResponse(savedChat);
    }

    @Transactional(readOnly = true)
    public Page<ChatResponse> getUserChats(Long userId, Pageable pageable) {
        Page<Chat> chats = chatRepository.findByParticipantsUserId(userId, pageable);
        return chats.map(chatMapper::toChatResponse);
    }

    @Transactional(readOnly = true)
    public ChatResponse getChat(Long chatId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        // Проверка, является ли пользователь участником чата
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant of this chat");
        }

        return chatMapper.toChatResponse(chat);
    }

    @Transactional
    public MessageResponse sendMessage(Long chatId, MessageRequest request, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        // Проверка, является ли пользователь участником чата
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant of this chat");
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .sentAt(ZonedDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // Обновление времени последнего сообщения в чате
        chat.setUpdatedAt(ZonedDateTime.now());
        chatRepository.save(chat);

        MessageResponse response = chatMapper.toMessageResponse(savedMessage);

        // Отправка сообщения через WebSocket
        webSocketService.sendToChat(chatId, response);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getChatMessages(Long chatId, Long currentUserId, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        // Проверка, является ли пользователь участником чата
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant of this chat");
        }

        Page<Message> messages = messageRepository.findByChatOrderBySentAtDesc(chat, pageable);
        return messages.map(chatMapper::toMessageResponse);
    }

    @Transactional
    public ChatResponse addParticipants(Long chatId, Set<Long> userIds, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        // Проверка, является ли пользователь администратором чата
        boolean isAdmin = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId) && "ADMIN".equals(p.getRole()));

        if (!isAdmin) {
            throw new AccessDeniedException("You don't have permission to add participants");
        }

        // Получение текущих участников
        Set<Long> existingParticipantIds = chat.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .collect(Collectors.toSet());

        // Добавление новых участников
        for (Long userId : userIds) {
            if (!existingParticipantIds.contains(userId)) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

                ChatParticipant participant = ChatParticipant.builder()
                        .chat(chat)
                        .user(user)
                        .role("MEMBER")
                        .joinedAt(ZonedDateTime.now())
                        .build();

                participantRepository.save(participant);
                chat.getParticipants().add(participant);
            }
        }

        return chatMapper.toChatResponse(chat);
    }

    @Transactional
    public void removeParticipant(Long chatId, Long userId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        // Проверка, является ли пользователь администратором чата или удаляет сам себя
        boolean isAdmin = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId) && "ADMIN".equals(p.getRole()));
        boolean isSelfRemoval = userId.equals(currentUserId);

        if (!isAdmin && !isSelfRemoval) {
            throw new AccessDeniedException("You don't have permission to remove participants");
        }

        // Поиск участника
        ChatParticipant participant = participantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        // Удаление участника
        participantRepository.delete(participant);
    }

    @Transactional
    public void deleteChat(Long chatId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        // Проверка, является ли пользователь администратором чата
        boolean isAdmin = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId) && "ADMIN".equals(p.getRole()));

        if (!isAdmin) {
            throw new AccessDeniedException("You don't have permission to delete this chat");
        }

        // Удаление всех сообщений
        messageRepository.deleteByChatId(chatId);

        // Удаление всех участников
        participantRepository.deleteByChatId(chatId);

        // Удаление чата
        chatRepository.delete(chat);
    }
}
