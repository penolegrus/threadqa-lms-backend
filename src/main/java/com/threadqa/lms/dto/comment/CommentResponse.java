package com.threadqa.lms.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userProfilePicture;
    private String content;
    private String entityType;
    private Long entityId;
    private Long parentId;
    private List<CommentResponse> replies;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean isEdited;
    private Integer replyCount;
}
