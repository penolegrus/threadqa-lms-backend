package com.lms.service;

import com.lms.dto.learningpath.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.LearningPathMapper;
import com.lms.model.*;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final FocusSkillRepository focusSkillRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LearningPathMapper learningPathMapper;

    @Transactional
    public LearningPathResponse createLearningPath(LearningPathRequest request) {
        LearningPath learningPath = LearningPath.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        LearningPath savedLearningPath = learningPathRepository.save(learningPath);

        return learningPathMapper.toLearningPathResponse(savedLearningPath, new ArrayList<>());
    }

    @Transactional(readOnly = true)
    public LearningPathResponse getLearningPath(Long learningPathId) {
        LearningPath learningPath = learningPathRepository.findById(learningPathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found"));

        List<Skill> skills = skillRepository.findByLearningPath(learningPath);
        List<SkillResponse> skillResponses = skills.stream()
                .map(learningPathMapper::toSkillResponse)
                .collect(Collectors.toList());

        return learningPathMapper.toLearningPathResponse(learningPath, skillResponses);
    }

    @Transactional(readOnly = true)
    public List<LearningPathResponse> getAllLearningPaths() {
        List<LearningPath> learningPaths = learningPathRepository.findAll();

        return learningPaths.stream()
                .map(learningPath -> {
                    List<Skill> skills = skillRepository.findByLearningPath(learningPath);
                    List<SkillResponse> skillResponses = skills.stream()
                            .map(learningPathMapper::toSkillResponse)
                            .collect(Collectors.toList());
                    return learningPathMapper.toLearningPathResponse(learningPath, skillResponses);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        LearningPath learningPath = learningPathRepository.findById(request.getLearningPathId())
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found"));

        Skill skill = Skill.builder()
                .name(request.getName())
                .description(request.getDescription())
                .skillLevel(request.getSkillLevel())
                .learningPath(learningPath)
                .build();

        Skill savedSkill = skillRepository.save(skill);

        // Add prerequisites if provided
        if (request.getPrerequisiteSkillIds() != null && !request.getPrerequisiteSkillIds().isEmpty()) {
            for (Long prerequisiteId : request.getPrerequisiteSkillIds()) {
                Skill prerequisiteSkill = skillRepository.findById(prerequisiteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Prerequisite skill not found"));

                SkillDependency dependency = SkillDependency.builder()
                        .skill(savedSkill)
                        .prerequisiteSkill(prerequisiteSkill)
                        .isRequired(true)
                        .build();

                savedSkill.getDependencies().add(dependency);
            }

            savedSkill = skillRepository.save(savedSkill);
        }

        // Add courses if provided
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            Set<Course> courses = new HashSet<>();
            for (Long courseId : request.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                courses.add(course);
            }

            savedSkill.setCourses(courses);
            savedSkill = skillRepository.save(savedSkill);
        }

        return learningPathMapper.toSkillResponse(savedSkill);
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkill(Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        return learningPathMapper.toSkillResponse(skill);
    }

    @Transactional
    public UserSkillResponse updateUserSkill(UserSkillRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        UserSkill userSkill = userSkillRepository.findByUserAndSkill(user, skill)
                .orElse(UserSkill.builder()
                        .user(user)
                        .skill(skill)
                        .createdAt(ZonedDateTime.now())
                        .build());

        userSkill.setProficiencyLevel(request.getProficiencyLevel());
        userSkill.setIsCompleted(request.getIsCompleted());

        if (request.getIsCompleted() && userSkill.getCompletedAt() == null) {
            userSkill.setCompletedAt(ZonedDateTime.now());
        } else if (!request.getIsCompleted()) {
            userSkill.setCompletedAt(null);
        }

        userSkill.setUpdatedAt(ZonedDateTime.now());

        UserSkill savedUserSkill = userSkillRepository.save(userSkill);

        return learningPathMapper.toUserSkillResponse(savedUserSkill);
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<UserSkill> userSkills = userSkillRepository.findByUser(user);

        return userSkills.stream()
                .map(learningPathMapper::toUserSkillResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkillsByLearningPath(Long userId, Long learningPathId) {
        learningPathRepository.findById(learningPathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<UserSkill> userSkills = userSkillRepository.findByUserAndLearningPath(userId, learningPathId);

        return userSkills.stream()
                .map(learningPathMapper::toUserSkillResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FocusSkillResponse addFocusSkill(FocusSkillRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Check if user has the required prerequisites
        List<Skill> prerequisites = skillRepository.findPrerequisiteSkills(skill.getId());
        for (Skill prerequisite : prerequisites) {
            UserSkill userSkill = userSkillRepository.findByUserAndSkill(user, prerequisite)
                    .orElse(null);

            if (userSkill == null || !userSkill.getIsCompleted()) {
                throw new IllegalStateException("You must complete the prerequisite skill: " + prerequisite.getName());
            }
        }

        FocusSkill focusSkill = FocusSkill.builder()
                .user(user)
                .skill(skill)
                .priority(request.getPriority())
                .createdAt(ZonedDateTime.now())
                .build();

        FocusSkill savedFocusSkill = focusSkillRepository.save(focusSkill);

        return learningPathMapper.toFocusSkillResponse(savedFocusSkill);
    }

    @Transactional
    public void removeFocusSkill(Long focusSkillId, Long userId) {
        FocusSkill focusSkill = focusSkillRepository.findById(focusSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("Focus skill not found"));

        if (!focusSkill.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to remove this focus skill");
        }

        focusSkillRepository.delete(focusSkill);
    }

    @Transactional(readOnly = true)
    public List<FocusSkillResponse> getUserFocusSkills(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<FocusSkill> focusSkills = focusSkillRepository.findByUserOrderByPriorityAsc(user);

        return focusSkills.stream()
                .map(learningPathMapper::toFocusSkillResponse)
                .collect(Collectors.toList());
    }
}