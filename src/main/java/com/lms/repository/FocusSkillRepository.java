package com.lms.repository;

import com.lms.model.FocusSkill;
import com.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FocusSkillRepository extends JpaRepository<FocusSkill, Long> {

    List<FocusSkill> findByUserOrderByPriorityAsc(User user);
}