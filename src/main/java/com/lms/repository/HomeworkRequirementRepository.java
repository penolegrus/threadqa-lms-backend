package com.lms.repository;

import com.lms.model.Homework;
import com.lms.model.HomeworkRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRequirementRepository extends JpaRepository<HomeworkRequirement, Long> {

    List<HomeworkRequirement> findByHomework(Homework homework);

    List<HomeworkRequirement> findByHomeworkId(Long homeworkId);
}