package com.threadqa.lms.repository.homework;

import com.threadqa.lms.model.homework.Homework;
import com.threadqa.lms.model.homework.HomeworkSubmission;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {

    List<HomeworkSubmission> findByHomework(Homework homework);

    Page<HomeworkSubmission> findByHomework(Homework homework, Pageable pageable);

    List<HomeworkSubmission> findByUser(User user);

    Page<HomeworkSubmission> findByUser(User user, Pageable pageable);

    Optional<HomeworkSubmission> findByHomeworkAndUser(Homework homework, User user);

    @Query("SELECT COUNT(hs) FROM HomeworkSubmission hs WHERE hs.homework.id = :homeworkId")
    Long countByHomeworkId(Long homeworkId);

    @Query("SELECT COUNT(hs) FROM HomeworkSubmission hs WHERE hs.homework.id = :homeworkId AND hs.status = 'COMPLETED'")
    Long countCompletedByHomeworkId(Long homeworkId);

    @Query("SELECT AVG(hs.score) FROM HomeworkSubmission hs WHERE hs.homework.id = :homeworkId AND hs.score IS NOT NULL")
    Double getAverageScoreByHomeworkId(Long homeworkId);

    @Query("SELECT COUNT(hs) FROM HomeworkSubmission hs WHERE hs.user.id = :userId")
    Long countByUserId(Long userId);

    @Query("SELECT COUNT(hs) FROM HomeworkSubmission hs WHERE hs.user.id = :userId AND hs.status = 'COMPLETED'")
    Long countCompletedByUserId(Long userId);

    @Query("SELECT AVG(hs.score) FROM HomeworkSubmission hs WHERE hs.user.id = :userId AND hs.score IS NOT NULL")
    Double getAverageScoreByUserId(Long userId);
}
