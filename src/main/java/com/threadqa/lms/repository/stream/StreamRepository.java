package com.threadqa.lms.repository.stream;

import com.threadqa.lms.model.stream.Stream;
import com.threadqa.lms.model.stream.StreamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {
    
    Page<Stream> findByCourseId(Long courseId, Pageable pageable);
    
    Page<Stream> findByInstructorId(Long instructorId, Pageable pageable);
    
    Page<Stream> findByStatus(StreamStatus status, Pageable pageable);
    
    @Query("SELECT s FROM Stream s WHERE s.scheduledStartTime BETWEEN :startDate AND :endDate")
    List<Stream> findByScheduledTimeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Stream s WHERE s.status = 'SCHEDULED' AND s.scheduledStartTime <= :timeThreshold")
    List<Stream> findUpcomingStreams(@Param("timeThreshold") LocalDateTime timeThreshold);
    
    @Query("SELECT s FROM Stream s WHERE s.status = 'LIVE'")
    List<Stream> findLiveStreams();
    
    Optional<Stream> findByIdAndInstructorId(Long id, Long instructorId);
}
