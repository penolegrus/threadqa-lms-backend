package com.threadqa.lms.repository.stream;

import com.threadqa.lms.model.stream.StreamParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamParticipantRepository extends JpaRepository<StreamParticipant, Long> {
    
    List<StreamParticipant> findByStreamId(Long streamId);
    
    List<StreamParticipant> findByUserId(Long userId);
    
    Optional<StreamParticipant> findByStreamIdAndUserId(Long streamId, Long userId);
    
    @Query("SELECT COUNT(sp) FROM StreamParticipant sp WHERE sp.stream.id = :streamId AND sp.isActive = true")
    Long countActiveParticipantsByStreamId(@Param("streamId") Long streamId);
    
    @Query("SELECT sp FROM StreamParticipant sp WHERE sp.stream.id = :streamId AND sp.isActive = true")
    List<StreamParticipant> findActiveParticipantsByStreamId(@Param("streamId") Long streamId);
}
