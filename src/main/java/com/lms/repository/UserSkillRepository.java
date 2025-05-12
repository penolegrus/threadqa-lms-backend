package com.lms.repository;

import com.lms.model.Skill;
import com.lms.model.User;
import com.lms.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(User user);

    Optional<UserSkill> findByUserAndSkill(User user, Skill skill);

    @Query("SELECT us FROM UserSkill us WHERE us.user.id = :userId AND us.skill.learningPath.id = :learningPathId")
    List<UserSkill> findByUserAndLearningPath(Long userId, Long learningPathId);
}