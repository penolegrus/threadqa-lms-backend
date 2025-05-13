package com.threadqa.lms.repository.homework;

import com.threadqa.lms.model.homework.HomeworkChatMessage;
import com.threadqa.lms.model.homework.HomeworkSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeworkChatMessageRepository extends JpaRepository<HomeworkChatMessage, Long> {

    Page<HomeworkChatMessage> findBySubmissionOrderBySentAtDesc(HomeworkSubmission submission, Pageable pageable);

    @Modifying
    @Query("UPDATE HomeworkChatMessage m SET m.isRead = true WHERE m.submission.id = :submissionId AND m.sender.id != :userId")
    void markAllAsReadInSubmission(Long submissionId, Long userId);

    @Query("SELECT COUNT(m) FROM HomeworkChatMessage m WHERE m.submission.id = :submissionId AND m.isRead = false AND m.sender.id != :userId")
    Long countUnreadBySubmissionIdAndUserId(Long submissionId, Long userId);
}
