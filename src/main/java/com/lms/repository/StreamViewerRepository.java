package com.lms.repository;

import com.lms.model.StreamViewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamViewerRepository extends JpaRepository<StreamViewer, Long> {

    @Query("SELECT sv FROM StreamViewer sv WHERE sv.stream.id = :streamId AND sv.user.id = :userId AND sv.leftAt IS NULL")
    Optional<StreamViewer> findActiveViewerByStreamAndUser(Long streamId, Long userId);

    @Query("SELECT COUNT(sv) FROM StreamViewer sv WHERE sv.stream.id = :streamId AND sv.leftAt IS NULL")
    Long countActiveViewersByStream(Long streamId);

    List<StreamViewer> findByStreamId(Long streamId);
}