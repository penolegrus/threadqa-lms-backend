package com.threadqa.lms.repository.chat;

import com.threadqa.lms.model.chat.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);

    @Modifying
    @Query("DELETE FROM ChatParticipant cp WHERE cp.chat.id = :chatId")
    void deleteByChatId(Long chatId);
}
