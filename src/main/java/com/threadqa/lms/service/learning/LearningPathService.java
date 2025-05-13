package com.threadqa.lms.service.learning;

import com.threadqa.lms.dto.learningpath.*;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.LearningPathMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.learning.LearningPath;
import com.threadqa.lms.model.learning.LearningPathItem;
import com.threadqa.lms.model.learning.UserLearningPathProgress;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.learning.LearningPathItemRepository;
import com.threadqa.lms.repository.learning.LearningPathRepository;
import com.threadqa.lms.repository.learning.UserLearningPathProgressRepository;
import com.threadqa.lms.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LearningPathService {
    
    @Autowired
    private LearningPathRepository learningPathRepository;
    
    @Autowired
    private LearningPathItemRepository learningPathItemRepository;
    
    @Autowired
    private UserLearningPathProgressRepository progressRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LearningPathMapper mapper;
    
    public Page<LearningPathResponse> getAllPublishedLearningPaths(Pageable pageable) {
        return learningPathRepository.findByPublishedTrue(pageable)
                .map(mapper::toResponse);
    }
    
    public Page<LearningPathResponse> getFeaturedLearningPaths(Pageable pageable) {
        return learningPathRepository.findByPublishedTrueAndFeaturedTrue(pageable)
                .map(mapper::toResponse);
    }
    
    public Page<LearningPathResponse> searchLearningPaths(String keyword, Pageable pageable) {
        return learningPathRepository.searchPublishedLearningPaths(keyword, pageable)
                .map(mapper::toResponse);
    }
    
    public LearningPathResponse getLearningPathBySlug(String slug) {
        LearningPath learningPath = learningPathRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with slug: " + slug));
        
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);
        
        return mapper.toResponseWithItems(learningPath, items);
    }
    
    public LearningPathResponse getLearningPathById(Long id) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + id));
        
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);
        
        return mapper.toResponseWithItems(learningPath, items);
    }
    
    @Transactional
    public LearningPathResponse createLearningPath(LearningPathRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if slug is already in use
        if (learningPathRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Learning path with slug '" + request.getSlug() + "' already exists");
        }
        
        LearningPath learningPath = mapper.toEntity(request, user);
        learningPath = learningPathRepository.save(learningPath);
        
        return mapper.toResponse(learningPath);
    }
    
    @Transactional
    public LearningPathResponse updateLearningPath(Long id, LearningPathRequest request, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + id));
        
        // Check if user is the creator of the learning path
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to update this learning path");
        }
        
        // Check if new slug is already in use by another learning path
        if (!learningPath.getSlug().equals(request.getSlug()) && 
            learningPathRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Learning path with slug '" + request.getSlug() + "' already exists");
        }
        
        learningPath.setTitle(request.getTitle());
        learningPath.setDescription(request.getDescription());
        learningPath.setSlug(request.getSlug());
        learningPath.setDifficultyLevel(request.getDifficultyLevel());
        learningPath.setEstimatedHours(request.getEstimatedHours());
        learningPath.setPublished(request.isPublished());
        learningPath.setFeatured(request.isFeatured());
        learningPath.setThumbnailUrl(request.getThumbnailUrl());
        learningPath.setTags(request.getTags());
        
        learningPath = learningPathRepository.save(learningPath);
        
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);
        
        return mapper.toResponseWithItems(learningPath, items);
    }
    
    @Transactional
    public void deleteLearningPath(Long id, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + id));
        
        // Check if user is the creator of the learning path
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to delete this learning path");
        }
        
        learningPathRepository.delete(learningPath);
    }
    
    @Transactional
    public LearningPathItemResponse addItemToLearningPath(Long pathId, LearningPathItemRequest request, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + pathId));
        
        // Check if user is the creator of the learning path
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to modify this learning path");
        }
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
        
        // Check if course is already in the learning path
        if (learningPathItemRepository.findByLearningPathOrderByPosition(learningPath).stream()
                .anyMatch(item -> item.getCourse().getId().equals(course.getId()))) {
            throw new BadRequestException("Course is already in the learning path");
        }
        
        // If position is not specified, add to the end
        if (request.getPosition() == null) {
            Integer maxPosition = learningPathItemRepository.findMaxPositionInPath(pathId);
            request.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        } else {
            // Shift existing items to make room for the new item
            learningPathItemRepository.incrementPositionsFromIndex(pathId, request.getPosition());
        }
        
        LearningPathItem item = mapper.toItemEntity(request, learningPath, course);
        item = learningPathItemRepository.save(item);
        
        return mapper.toItemResponse(item);
    }
    
    @Transactional
    public LearningPathItemResponse updateLearningPathItem(Long pathId, Long itemId, LearningPathItemRequest request, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + pathId));
        
        // Check if user is the creator of the learning path
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to modify this learning path");
        }
        
        LearningPathItem item = learningPathItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path item not found with id: " + itemId));
        
        // Ensure the item belongs to the specified learning path
        if (!item.getLearningPath().getId().equals(pathId)) {
            throw new BadRequestException("Item does not belong to the specified learning path");
        }
        
        // If position is changing, handle reordering
        if (request.getPosition() != null && !request.getPosition().equals(item.getPosition())) {
            // Remove from old position
            learningPathItemRepository.decrementPositionsAfterIndex(pathId, item.getPosition());
            
            // Insert at new position
            learningPathItemRepository.incrementPositionsFromIndex(pathId, request.getPosition());
            
            item.setPosition(request.getPosition());
        }
        
        // Update other fields
        item.setRequired(request.isRequired());
        item.setNotes(request.getNotes());
        
        // If course is changing, update it
        if (!item.getCourse().getId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
            
            // Check if new course is already in the learning path
            if (learningPathItemRepository.findByLearningPathOrderByPosition(learningPath).stream()
                    .anyMatch(i -> i.getCourse().getId().equals(newCourse.getId()) && !i.getId().equals(itemId))) {
                throw new BadRequestException("Course is already in the learning path");
            }
            
            item.setCourse(newCourse);
        }
        
        item = learningPathItemRepository.save(item);
        
        return mapper.toItemResponse(item);
    }
    
    @Transactional
    public void removeLearningPathItem(Long pathId, Long itemId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + pathId));
        
        // Check if user is the creator of the learning path
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to modify this learning path");
        }
        
        LearningPathItem item = learningPathItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path item not found with id: " + itemId));
        
        // Ensure the item belongs to the specified learning path
        if (!item.getLearningPath().getId().equals(pathId)) {
            throw new BadRequestException("Item does not belong to the specified learning path");
        }
        
        // Decrement positions of items after the removed item
        learningPathItemRepository.decrementPositionsAfterIndex(pathId, item.getPosition());
        
        // Delete the item
        learningPathItemRepository.delete(item);
    }
    
    @Transactional
    public UserLearningPathProgressResponse enrollUserInLearningPath(Long pathId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + pathId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user is already enrolled
        if (progressRepository.findByUserAndLearningPath(user, learningPath).isPresent()) {
            throw new BadRequestException("User is already enrolled in this learning path");
        }
        
        // Create progress record
        UserLearningPathProgress progress = UserLearningPathProgress.builder()
                .user(user)
                .learningPath(learningPath)
                .progressPercentage(0)
                .completed(false)
                .build();
        
        progress = progressRepository.save(progress);
        
        // Auto-enroll in all required courses in the learning path
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);
        
        for (LearningPathItem item : items) {
            if (item.isRequired()) {
                Course course = item.getCourse();
                
                // Check if user is already enrolled in the course
                if (courseEnrollmentRepository.findByUserAndCourse(user, course).isEmpty()) {
                    // Enroll user in the course
                    CourseEnrollment enrollment = CourseEnrollment.builder()
                            .user(user)
                            .course(course)
                            .enrollmentDate(LocalDateTime.now())
                            .active(true)
                            .build();
                    
                    courseEnrollmentRepository.save(enrollment);
                }
            }
        }
        
        return mapper.toProgressResponse(progress, items.size(), 0);
    }
    
    public UserLearningPathProgressResponse getUserLearningPathProgress(Long pathId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + pathId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        UserLearningPathProgress progress = progressRepository.findByUserAndLearningPath(user, learningPath)
                .orElseThrow(() -> new ResourceNotFoundException("User is not enrolled in this learning path"));
        
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);
        
        // Calculate completed items
        int completedItems = 0;
        for (LearningPathItem item : items) {
            Course course = item.getCourse();
            
            // Check if user has completed the course
            courseEnrollmentRepository.findByUserAndCourse(user, course)
                    .ifPresent(enrollment -> {
                        if (enrollment.getCompletionDate() != null) {
                            completedItems++;
                        }
                    });
        }
        
        return mapper.toDetailedProgressResponse(progress, items.size(), completedItems, items);
    }
    
    public List<UserLearningPathProgressResponse> getUserEnrollments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<UserLearningPathProgress> progressList = progressRepository.findByUser(user);
        
        // Get all learning paths the user is enrolled in
        List<LearningPath> learningPaths = progressList.stream()
                .map(UserLearningPathProgress::getLearningPath)
                .collect(Collectors.toList());
        
        // Get all items for these learning paths
        Map<Long, List<LearningPathItem>> itemsByPathId = learningPathItemRepository.findAll().stream()
                .filter(item -> learningPaths.contains(item.getLearningPath()))
                .collect(Collectors.groupingBy(item -> item.getLearningPath().getId()));
        
        // Get all course enrollments for the user
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByUser(user);
        Map<Long, CourseEnrollment> enrollmentsByCourseId = enrollments.stream()
                .collect(Collectors.toMap(e -> e.getCourse().getId(), e -> e));
        
        return progressList.stream()
                .map(progress -> {
                    Long pathId = progress.getLearningPath().getId();
                    List<LearningPathItem> items = itemsByPathId.getOrDefault(pathId, List.of());
                    
                    // Count completed courses
                    int completedItems = (int) items.stream()
                            .filter(item -> {
                                CourseEnrollment enrollment = enrollmentsByCourseId.get(item.getCourse().getId());
                                return enrollment != null && enrollment.getCompletionDate() != null;
                            })
                            .count();
                    
                    return mapper.toProgressResponse(progress, items.size(), completedItems);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserLearningPathProgressResponse updateUserProgress(Long pathId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + pathId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        UserLearningPathProgress progress = progressRepository.findByUserAndLearningPath(user, learningPath)
                .orElseThrow(() -> new ResourceNotFoundException("User is not enrolled in this learning path"));
        
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);
        
        // Calculate completed items and update progress
        int totalItems = items.size();
        int completedItems = 0;
        
        for (LearningPathItem item : items) {
            Course course = item.getCourse();
            
            // Check if user has completed the course
            courseEnrollmentRepository.findByUserAndCourse(user, course)
                    .ifPresent(enrollment -> {
                        if (enrollment.getCompletionDate() != null) {
                            completedItems++;
                        }
                    });
        }
        
        // Update progress percentage
        int progressPercentage = totalItems > 0 ? (completedItems * 100) / totalItems : 0;
        progress.setProgressPercentage(progressPercentage);
        
        // Check if all required items are completed
        boolean allRequiredCompleted = items.stream()
                .filter(LearningPathItem::isRequired)
                .allMatch(item -> {
                    CourseEnrollment enrollment = courseEnrollmentRepository
                            .findByUserAndCourse(user, item.getCourse())
                            .orElse(null);
                    return enrollment != null && enrollment.getCompletionDate() != null;
                });
        
        // Mark as completed if all required items are done
        if (allRequiredCompleted && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }
        
        progress = progressRepository.save(progress);
        
        return mapper.toProgressResponse(progress, totalItems, completedItems);
    }
}
