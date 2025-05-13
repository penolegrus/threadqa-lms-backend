package com.threadqa.lms.service.achievement;

import com.threadqa.lms.dto.achievement.AchievementRequest;
import com.threadqa.lms.dto.achievement.AchievementResponse;
import com.threadqa.lms.dto.achievement.UserAchievementResponse;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.AchievementMapper;
import com.threadqa.lms.model.achievement.Achievement;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.model.user.UserAchievement;
import com.threadqa.lms.repository.achievement.AchievementRepository;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.homework.HomeworkSubmissionRepository;
import com.threadqa.lms.repository.user.UserAchievementRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final AchievementMapper achievementMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements() {
        List<Achievement> achievements = achievementRepository.findAll();
        return achievements.stream()
                .map(achievementMapper::toAchievementResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AchievementResponse getAchievement(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));
        return achievementMapper.toAchievementResponse(achievement);
    }

    @Transactional
    public AchievementResponse createAchievement(AchievementRequest request) {
        // Проверка уникальности имени достижения
        if (achievementRepository.existsByName(request.getName())) {
            throw new BadRequestException("Achievement with this name already exists");
        }

        Achievement achievement = Achievement.builder()
                .name(request.getName())
                .title(request.getTitle())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .type(request.getType())
                .threshold(request.getThreshold())
                .xpReward(request.getXpReward())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Achievement savedAchievement = achievementRepository.save(achievement);
        return achievementMapper.toAchievementResponse(savedAchievement);
    }

    @Transactional
    public AchievementResponse updateAchievement(Long achievementId, AchievementRequest request) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        // Проверка уникальности имени достижения, если оно изменилось
        if (!achievement.getName().equals(request.getName()) && 
            achievementRepository.existsByName(request.getName())) {
            throw new BadRequestException("Achievement with this name already exists");
        }

        achievement.setName(request.getName());
        achievement.setTitle(request.getTitle());
        achievement.setDescription(request.getDescription());
        achievement.setIconUrl(request.getIconUrl());
        achievement.setType(request.getType());
        achievement.setThreshold(request.getThreshold());
        achievement.setXpReward(request.getXpReward());
        achievement.setUpdatedAt(ZonedDateTime.now());

        Achievement updatedAchievement = achievementRepository.save(achievement);
        return achievementMapper.toAchievementResponse(updatedAchievement);
    }

    @Transactional
    public void deleteAchievement(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        // Удаление связанных записей о достижениях пользователей
        List<UserAchievement> userAchievements = userAchievementRepository.findAll().stream()
                .filter(ua -> ua.getAchievement().getId().equals(achievementId))
                .collect(Collectors.toList());
        
        userAchievementRepository.deleteAll(userAchievements);

        // Удаление достижения
        achievementRepository.delete(achievement);
    }

    @Transactional(readOnly = true)
    public Page<UserAchievementResponse> getUserAchievements(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserAchievement> userAchievements = userAchievementRepository.findByUser(user, pageable);

        return userAchievements.map(achievementMapper::toUserAchievementResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserAchievementResponse> getUserCompletedAchievements(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserAchievement> userAchievements = userAchievementRepository.findByUserAndIsCompletedTrue(user, pageable);

        return userAchievements.map(achievementMapper::toUserAchievementResponse);
    }

    @Transactional(readOnly = true)
    public Integer getUserTotalXp(Long userId) {
        Integer totalXp = userAchievementRepository.getTotalXpByUserId(userId);
        return totalXp != null ? totalXp : 0;
    }

    @Transactional
    public void checkAndUpdateAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получение всех достижений
        List<Achievement> achievements = achievementRepository.findAll();

        for (Achievement achievement : achievements) {
            // Проверка и обновление прогресса для каждого достижения
            updateAchievementProgress(user, achievement);
        }
    }

    @Async
    @Transactional
    public void checkCourseCompletionAchievements(Long userId) {
        // Получение пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получение достижений типа COURSE_COMPLETION
        List<Achievement> achievements = achievementRepository.findByType(Achievement.AchievementType.COURSE_COMPLETION);

        // Подсчет завершенных курсов
        Long completedCourses = courseEnrollmentRepository.countByCompletedAtIsNotNullAndUserId(userId);

        for (Achievement achievement : achievements) {
            // Проверка, достиг ли пользователь порога для этого достижения
            if (completedCourses >= achievement.getThreshold()) {
                // Обновление или создание записи о достижении пользователя
                updateOrCreateUserAchievement(user, achievement, completedCourses.intValue());
            }
        }
    }

    @Async
    @Transactional
    public void checkCourseEnrollmentAchievements(Long userId) {
        // Получение пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получение достижений типа COURSE_ENROLLMENT
        List<Achievement> achievements = achievementRepository.findByType(Achievement.AchievementType.COURSE_ENROLLMENT);

        // Подсчет зачислений на курсы
        Long enrolledCourses = courseEnrollmentRepository.countByUserId(userId);

        for (Achievement achievement : achievements) {
            // Проверка, достиг ли пользователь порога для этого достижения
            if (enrolledCourses >= achievement.getThreshold()) {
                // Обновление или создание записи о достижении пользователя
                updateOrCreateUserAchievement(user, achievement, enrolledCourses.intValue());
            }
        }
    }

    @Async
    @Transactional
    public void checkHomeworkCompletionAchievements(Long userId) {
        // Получение пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получение достижений типа HOMEWORK_COMPLETION
        List<Achievement> achievements = achievementRepository.findByType(Achievement.AchievementType.HOMEWORK_COMPLETION);

        // Подсчет завершенных домашних заданий
        Long completedHomeworks = homeworkSubmissionRepository.countCompletedByUserId(userId);

        for (Achievement achievement : achievements) {
            // Проверка, достиг ли пользователь порога для этого достижения
            if (completedHomeworks >= achievement.getThreshold()) {
                // Обновление или создание записи о достижении пользователя
                updateOrCreateUserAchievement(user, achievement, completedHomeworks.intValue());
            }
        }
    }

    private void updateAchievementProgress(User user, Achievement achievement) {
        // Получение текущего прогресса пользователя по достижению
        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);

        // Если достижение уже выполнено, пропускаем
        if (userAchievementOpt.isPresent() && userAchievementOpt.get().getIsCompleted()) {
            return;
        }

        // Вычисление текущего прогресса в зависимости от типа достижения
        int currentProgress = calculateProgress(user, achievement);

        // Обновление или создание записи о достижении пользователя
        updateOrCreateUserAchievement(user, achievement, currentProgress);
    }

    private int calculateProgress(User user, Achievement achievement) {
        switch (achievement.getType()) {
            case COURSE_COMPLETION:
                return courseEnrollmentRepository.countByCompletedAtIsNotNullAndUserId(user.getId()).intValue();
            case COURSE_ENROLLMENT:
                return courseEnrollmentRepository.countByUserId(user.getId()).intValue();
            case HOMEWORK_COMPLETION:
                return homeworkSubmissionRepository.countCompletedByUserId(user.getId()).intValue();
            // Добавьте другие типы достижений по мере необходимости
            default:
                return 0;
        }
    }

    private void updateOrCreateUserAchievement(User user, Achievement achievement, int currentProgress) {
        UserAchievement userAchievement;
        boolean isNewlyCompleted = false;

        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserAndAchievement(user, achievement);

        if (userAchievementOpt.isPresent()) {
            userAchievement = userAchievementOpt.get();
            
            // Обновление прогресса
            userAchievement.setProgress(currentProgress);
            
            // Проверка, достигнут ли порог
            if (!userAchievement.getIsCompleted() && currentProgress >= achievement.getThreshold()) {
                userAchievement.setIsCompleted(true);
                userAchievement.setEarnedAt(ZonedDateTime.now());
                isNewlyCompleted = true;
            }
        } else {
            // Создание новой записи
            userAchievement = UserAchievement.builder()
                    .user(user)
                    .achievement(achievement)
                    .progress(currentProgress)
                    .isCompleted(currentProgress >= achievement.getThreshold())
                    .isNotified(false)
                    .build();
            
            if (userAchievement.getIsCompleted()) {
                userAchievement.setEarnedAt(ZonedDateTime.now());
                isNewlyCompleted = true;
            }
        }

        UserAchievement savedUserAchievement = userAchievementRepository.save(userAchievement);

        // Отправка уведомления, если достижение только что выполнено
        if (isNewlyCompleted && !savedUserAchievement.getIsNotified()) {
            notificationService.createNotification(
                    user.getId(),
                    "Новое достижение!",
                    "Вы получили достижение \"" + achievement.getTitle() + "\"",
                    "ACHIEVEMENT",
                    "/achievements"
            );
            
            savedUserAchievement.setIsNotified(true);
            userAchievementRepository.save(savedUserAchievement);
        }
    }
}
