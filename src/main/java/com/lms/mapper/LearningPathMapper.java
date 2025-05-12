package com.lms.mapper;

import com.lms.dto.learningpath.*;
import com.lms.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningPathMapper {

    public LearningPathResponse toLearningPathResponse(LearningPath learningPath, List<SkillResponse> skills) {
        if (learningPath == null) {
            return null;
        }

        return LearningPathResponse.builder()
                .id(learningPath.getId())
                .name(learningPath.getName())
                .description(learningPath.getDescription())
                .createdAt(learningPath.getCreatedAt())
                .updatedAt(learningPath.getUpdatedAt())
                .skills(skills)
                .build();
    }

    public SkillResponse toSkillResponse(Skill skill) {
        if (skill == null) {
            return null;
        }

        List<SkillDependencyResponse> prerequisites = skill.getDependencies().stream()
                .map(this::toSkillDependencyResponse)
                .collect(Collectors.toList());

        List<Long> courseIds = skill.getCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        List<String> courseNames = skill.getCourses().stream()
                .map(Course::getTitle)
                .collect(Collectors.toList());

        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .skillLevel(skill.getSkillLevel())
                .learningPathId(skill.getLearningPath().getId())
                .prerequisites(prerequisites)
                .courseIds(courseIds)
                .courseNames(courseNames)
                .build();
    }

    public SkillDependencyResponse toSkillDependencyResponse(SkillDependency dependency) {
        if (dependency == null) {
            return null;
        }

        return SkillDependencyResponse.builder()
                .skillId(dependency.getPrerequisiteSkill().getId())
                .skillName(dependency.getPrerequisiteSkill().getName())
                .isRequired(dependency.getIsRequired())
                .build();
    }

    public UserSkillResponse toUserSkillResponse(UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }

        return UserSkillResponse.builder()
                .id(userSkill.getId())
                .userId(userSkill.getUser().getId())
                .skillId(userSkill.getSkill().getId())
                .skillName(userSkill.getSkill().getName())
                .skillDescription(userSkill.getSkill().getDescription())
                .skillLevel(userSkill.getSkill().getSkillLevel())
                .proficiencyLevel(userSkill.getProficiencyLevel())
                .isCompleted(userSkill.getIsCompleted())
                .completedAt(userSkill.getCompletedAt())
                .createdAt(userSkill.getCreatedAt())
                .updatedAt(userSkill.getUpdatedAt())
                .build();
    }

    public FocusSkillResponse toFocusSkillResponse(FocusSkill focusSkill) {
        if (focusSkill == null) {
            return null;
        }

        return FocusSkillResponse.builder()
                .id(focusSkill.getId())
                .userId(focusSkill.getUser().getId())
                .skillId(focusSkill.getSkill().getId())
                .skillName(focusSkill.getSkill().getName())
                .priority(focusSkill.getPriority())
                .createdAt(focusSkill.getCreatedAt())
                .build();
    }
}