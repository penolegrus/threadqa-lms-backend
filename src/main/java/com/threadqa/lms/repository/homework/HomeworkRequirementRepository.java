package com.threadqa.lms.repository.homework;

import com.threadqa.lms.model.homework.Homework;
import com.threadqa.lms.model.homework.HomeworkRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRequirementRepository extends JpaRepository<HomeworkRequirement, Long> {

    List<HomeworkRequirement> findByHomework(Homework homework);

    List<HomeworkRequirement> findByHomeworkOrderByOrderIndexAsc(Homework homework);

    void deleteByHomeworkId(Long homeworkId);
}
