package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.comment.CommentResponse;
import com.threadqa.lms.model.comment.Comment;
import com.threadqa.lms.repository.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final CommentRepository commentRepository;

    public CommentResponse toCommentResponse(Comment comment, boolean includeReplies) {
        if (comment == null) {
            return null;
        }

        Integer replyCount = commentRepository.countRepliesByParentId(comment.getId());

        List<CommentResponse> replies = null;
        if (includeReplies && !comment.getReplies().isEmpty()) {
            replies = comment.getReplies().stream()
                    .map(reply -> toCommentResponse(reply, false))
                    .collect(Collectors.toList());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .userProfilePicture(comment.getUser().getProfilePicture())
                .content(comment.getContent())
                .entityType(comment.getEntityType())
                .entityId(comment.getEntityId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(replies)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isEdited(comment.getIsEdited())
                .replyCount(replyCount)
                .build();
    }
}
