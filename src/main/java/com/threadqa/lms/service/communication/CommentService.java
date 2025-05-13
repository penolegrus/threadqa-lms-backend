package com.threadqa.lms.service.communication;

import com.threadqa.lms.dto.comment.CommentRequest;
import com.threadqa.lms.dto.comment.CommentResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.CommentMapper;
import com.threadqa.lms.model.comment.Comment;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.comment.CommentRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(CommentRequest request, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .isEdited(false)
                .build();

        // Если это ответ на комментарий
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParent(parentComment);

            // Отправка уведомления автору родительского комментария
            if (!parentComment.getUser().getId().equals(currentUserId)) {
                notificationService.createNotification(
                        parentComment.getUser().getId(),
                        "Новый ответ на ваш комментарий",
                        user.getFirstName() + " " + user.getLastName() + " ответил на ваш комментарий",
                        "COMMENT_REPLY",
                        "/comments/" + parentComment.getId()
                );
            }
        }

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentResponse(savedComment, false);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByEntity(String entityType, Long entityId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findRootCommentsByEntityTypeAndEntityId(entityType, entityId, pageable);

        return comments.map(comment -> commentMapper.toCommentResponse(comment, true));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesByParentId(Long parentId) {
        List<Comment> replies = commentRepository.findRepliesByParentId(parentId);

        return replies.stream()
                .map(reply -> commentMapper.toCommentResponse(reply, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        return commentMapper.toCommentResponse(comment, true);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Проверка прав доступа - только автор комментария может его редактировать
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this comment");
        }

        comment.setContent(request.getContent());
        comment.setUpdatedAt(ZonedDateTime.now());
        comment.setIsEdited(true);

        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toCommentResponse(updatedComment, true);
    }

    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Проверка прав доступа - только автор комментария может его удалить
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Integer countCommentsByEntity(String entityType, Long entityId) {
        return commentRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }
}
