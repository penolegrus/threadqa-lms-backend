package com.lms.repository;

import com.lms.model.HomeworkChatMessage;
import com.lms.model.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkChatMessageRepository extends JpaRepository<HomeworkChatMessage, Long> {

    List<HomeworkChatMessage> findByHomeworkSubmissionOrderBySentAtAsc(HomeworkSubmission submission);

    List<HomeworkChatMessage> findByHomeworkSubmissionId(Long submissionId);
}