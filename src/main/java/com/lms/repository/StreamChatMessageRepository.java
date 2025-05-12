package com.lms.repository;

import com.lms.model.StreamChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamChatMessageRepository extends JpaRepository<StreamChatMessage, Long> {

    Page<StreamChatMessage> findByStreamIdOrderBySentAtAsc(Long streamId, Pageable pageable);

    Page<StreamChatMessage> findByStreamIdAndIsPinnedTrueOrderBySentAtAsc(Long streamId, Pageable pageable);
}