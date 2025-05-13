package com.threadqa.lms.service.progress;

import com.threadqa.lms.dto.progress.*;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.ProgressMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.progress.UserActivity;
import com.threadqa.lms.model.progress.UserEngagement;
import com.threadqa.lms.model.progress.UserProgress;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.course.TopicRepository;
import com.threadqa.lms.repository.progress.UserActivityRepository;
import com.threadqa.lms.repository.progress.UserEngagementRepository;
import com.threadqa.lms.repository.progress.UserProgressRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressTrackingService {

    private final UserProgressRepository progressRepository;
    private final UserActivityRepository activityRepository;
    private final UserEngagementRepository engagementRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final ProgressMapper progressMapper;

    @Transactional
    public UserProgressResponse trackProgress(UserProgressRequest request, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Проверка, существует ли уже запись о прогрессе
        Optional<UserProgress> existingProgress = progressRepository.findByUserAndCourseAndTopicAndContentTypeAndContentId(
                user, course, topic, request.getContentType(), request.getContentId());

        UserProgress userProgress;

        if (existingProgress.isPresent()) {
            // Обновление существующей записи
            userProgress = existingProgress.get();
            
            if (request.getIsCompleted() != null) {
                userProgress.setIsCompleted(request.getIsCompleted());
                
                if (Boolean.TRUE.equals(request.getIsCompleted()) && userProgress.getCompletedAt() == null) {
                    userProgress.setCompletedAt(ZonedDateTime.now());
                }
            }
            
            if (request.getProgressPercentage() != null) {
                userProgress.setProgressPercentage(request.getProgressPercentage());
            }
            
            if (request.getTimeSpentSeconds() != null) {
                // Добавляем новое время к существующему
                Long currentTimeSpent = userProgress.getTimeSpentSeconds() != null ? 
                        userProgress.getTimeSpentSeconds() : 0L;
                userProgress.setTimeSpentSeconds(currentTimeSpent + request.getTimeSpentSeconds());
            }
            
            userProgress.setLastAccessedAt(ZonedDateTime.now());
            userProgress.setUpdatedAt(ZonedDateTime.now());
        } else {
            // Создание новой записи
            userProgress = UserProgress.builder()
                    .user(user)
                    .course(course)
                    .topic(topic)
                    .contentType(request.getContentType())
                    .contentId(request.getContentId())
                    .isCompleted(request.getIsCompleted() != null ? request.getIsCompleted() : false)
                    .progressPercentage(request.getProgressPercentage())
                    .timeSpentSeconds(request.getTimeSpentSeconds())
                    .lastAccessedAt(ZonedDateTime.now())
                    .completedAt(Boolean.TRUE.equals(request.getIsCompleted()) ? ZonedDateTime.now() : null)
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();
        }

        UserProgress savedProgress = progressRepository.save(userProgress);
        return progressMapper.toUserProgressResponse(savedProgress);
    }

    @Transactional
    public UserActivityResponse trackActivity(UserActivity.ActivityType activityType, String entityType, 
                                             Long entityId, String description, String ipAddress, 
                                             String userAgent, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserActivity userActivity = UserActivity.builder()
                .user(user)
                .activityType(activityType)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(ZonedDateTime.now())
                .build();

        UserActivity savedActivity = activityRepository.save(userActivity);
        return progressMapper.toUserActivityResponse(savedActivity);
    }

    @Transactional
    public UserEngagementResponse trackEngagement(Long courseId, Long sessionDurationSeconds, 
                                                 Integer pageViews, Integer interactions, 
                                                 Integer comments, Integer questionsAsked, 
                                                 Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        }

        UserEngagement userEngagement = UserEngagement.builder()
                .user(user)
                .course(course)
                .engagementDate(ZonedDateTime.now())
                .sessionDurationSeconds(sessionDurationSeconds)
                .pageViews(pageViews)
                .interactions(interactions)
                .comments(comments)
                .questionsAsked(questionsAsked)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        UserEngagement savedEngagement = engagementRepository.save(userEngagement);
        return progressMapper.toUserEngagementResponse(savedEngagement);
    }

    @Transactional(readOnly = true)
    public Page<UserProgressResponse> getUserProgressByUser(Long userId, Pageable pageable, Long currentUserId) {
        // Проверка прав доступа - только сам пользователь или инструктор/админ может просматривать прогресс
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view this user's progress");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserProgress> progressPage = progressRepository.findByUser(user, pageable);
        return progressPage.map(progressMapper::toUserProgressResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserProgressResponse> getUserProgressByCourse(Long userId, Long courseId, Pageable pageable, Long currentUserId) {
        // Проверка прав доступа - только сам пользователь или инструктор курса может просматривать прогресс
        if (!userId.equals(currentUserId)) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
            if (!course.getInstructor().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't have permission to view this user's progress");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Page<UserProgress> progressPage = progressRepository.findByUserAndCourse(user, course, pageable);
        return progressPage.map(progressMapper::toUserProgressResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getUserActivities(Long userId, Pageable pageable, Long currentUserId) {
        // Проверка прав доступа - только сам пользователь или админ может просматривать активность
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view this user's activities");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserActivity> activityPage = activityRepository.findByUser(user, pageable);
        return activityPage.map(progressMapper::toUserActivityResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserEngagementResponse> getUserEngagements(Long userId, Pageable pageable, Long currentUserId) {
        // Проверка прав доступа - только сам пользователь или админ может просматривать вовлеченность
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view this user's engagements");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserEngagement> engagementPage = engagementRepository.findByUser(user, pageable);
        return engagementPage.map(progressMapper::toUserEngagementResponse);
    }

    @Transactional(readOnly = true)
    public UserProgressSummaryResponse getUserProgressSummary(Long userId, Long courseId, Long currentUserId) {
        // Проверка прав доступа - только сам пользователь или инструктор курса может просматривать сводку прогресса
        if (!userId.equals(currentUserId)) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
            if (!course.getInstructor().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't have permission to view this user's progress summary");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Получение данных о прогрессе
        Long completedItems = progressRepository.countCompletedItemsByCourseAndUser(courseId, userId);
        Long totalItems = progressRepository.countTotalItemsByCourse(courseId);
        Double completionPercentage = totalItems > 0 ? (completedItems * 100.0) / totalItems : 0.0;
        Long totalTimeSpent = progressRepository.getTotalTimeSpentByCourseAndUser(courseId, userId);

        // Получение данных о вовлеченности
        Long totalPageViews = engagementRepository.getTotalPageViewsByUser(userId);
        Long totalInteractions = engagementRepository.getTotalInteractionsByUser(userId);
        
        // Получение данных о последней активности
        Page<UserActivity> lastActivities = activityRepository.findByUser(user, Pageable.ofSize(1));
        String lastActivity = "";
        String lastActivityDate = "";
        
        if (!lastActivities.isEmpty()) {
            UserActivity lastAct = lastActivities.getContent().get(0);
            lastActivity = lastAct.getActivityType().toString();
            lastActivityDate = lastAct.getCreatedAt().toString();
        }

        // Получение количества логинов
        Long totalLogins = activityRepository.countByUserAndActivityType(userId, UserActivity.ActivityType.LOGIN);

        return UserProgressSummaryResponse.builder()
                .userId(userId)
                .userName(user.getFirstName() + " " + user.getLastName())
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .completedItems(completedItems != null ? completedItems.intValue() : 0)
                .totalItems(totalItems != null ? totalItems.intValue() : 0)
                .completionPercentage(completionPercentage)
                .totalTimeSpentSeconds(totalTimeSpent != null ? totalTimeSpent : 0L)
                .totalPageViews(totalPageViews != null ? totalPageViews.intValue() : 0)
                .totalInteractions(totalInteractions != null ? totalInteractions.intValue() : 0)
                .totalLogins(totalLogins != null ? totalLogins.intValue() : 0)
                .lastActivity(lastActivity)
                .lastActivityDate(lastActivityDate)
                .build();
    }
}
