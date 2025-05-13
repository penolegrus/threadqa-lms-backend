package com.threadqa.lms.repository.chat;

import com.threadqa.lms.model.chat.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<Chat> findByParticipantsUserId(Long userId, Pageable pageable);
}
