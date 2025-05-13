package com.threadqa.lms.repository.comment;

import com.threadqa.lms.model.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.entityType = :entityType AND c.entityId = :entityId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findRootCommentsByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(Long parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :commentId")
    Integer countRepliesByParentId(Long commentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.entityType = :entityType AND c.entityId = :entityId")
    Integer countByEntityTypeAndEntityId(String entityType, Long entityId);
}
