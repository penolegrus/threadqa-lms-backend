package com.lms.repository;

import com.lms.model.Stream;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {

    Page<Stream> findByCourseId(Long courseId, Pageable pageable);

    Page<Stream> findByInstructor(User instructor, Pageable pageable);

    Optional<Stream> findByStreamKey(String streamKey);

    @Query("SELECT s FROM Stream s WHERE s.isLive = true")
    List<Stream> findAllLiveStreams();

    @Query("SELECT s FROM Stream s WHERE s.scheduledStartTime > :now ORDER BY s.scheduledStartTime ASC")
    List<Stream> findUpcomingStreams(ZonedDateTime now, Pageable pageable);

    @Query("SELECT s FROM Stream s WHERE s.isRecorded = true AND s.recordingUrl IS NOT NULL")
    Page<Stream> findAllRecordedStreams(Pageable pageable);
}