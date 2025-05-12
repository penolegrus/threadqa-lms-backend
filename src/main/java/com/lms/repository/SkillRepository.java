package com.lms.repository;

import com.lms.model.LearningPath;
import com.lms.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByLearningPath(LearningPath learningPath);

    @Query("SELECT s FROM Skill s WHERE s.id IN " +
            "(SELECT sd.prerequisiteSkill.id FROM SkillDependency sd WHERE sd.skill.id = :skillId)")
    List<Skill> findPrerequisiteSkills(Long skillId);
}