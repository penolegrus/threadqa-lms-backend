package com.lms.repository;

import com.lms.model.Homework;
import com.lms.model.HomeworkSubmission;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {

    Page<HomeworkSubmission> findByHomework(Homework homework, Pageable pageable);

    Page<HomeworkSubmission> findByUser(User user, Pageable pageable);

    Optional<HomeworkSubmission> findByHomeworkAndUser(Homework homework, User user);

    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);
}