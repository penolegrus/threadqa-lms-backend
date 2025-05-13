package com.threadqa.lms.service.analytics;

import com.threadqa.lms.dto.analytics.CourseAnalyticsResponse;
import com.threadqa.lms.dto.analytics.SystemAnalyticsResponse;
import com.threadqa.lms.dto.analytics.UserAnalyticsResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.repository.assessment.TestSubmissionRepository;
import com.threadqa.lms.repository.course.CategoryRepository;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.course.CourseReviewRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final TestSubmissionRepository testSubmissionRepository;

    @Transactional(readOnly = true)
    public SystemAnalyticsResponse getSystemAnalytics() {
        // Агрегация данных о пользователях
        Integer totalUsers = (int) userRepository.count();
        Integer studentCount = userRepository.countByRoleName("ROLE_STUDENT").intValue();
        Integer instructorCount = userRepository.countByRoleName("ROLE_INSTRUCTOR").intValue();
        
        // Активные пользователи за последние 30 дней
        ZonedDateTime thirtyDaysAgo = ZonedDateTime.now().minusDays(30);
        Integer activeUsers = userRepository.countActiveUsersSince(thirtyDaysAgo).intValue();
        
        // Агрегация данных о курсах
        Integer totalCourses = (int) courseRepository.count();
        Integer publishedCourses = courseRepository.countByIsPublishedTrue().intValue();
        
        // Агрегация данных о зачислениях
        Integer totalEnrollments = (int) enrollmentRepository.count();
        Integer completedCourses = enrollmentRepository.countByCompletedAtIsNotNull().intValue();
        
        // Вычисление ставки завершения по системе
        Double systemWideCompletionRate = totalEnrollments > 0 ? 
                (double) completedCourses / totalEnrollments * 100 : 0.0;
        
        // Средний рейтинг по всем курсам
        Double averageRating = reviewRepository.getSystemWideAverageRating();
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // Статистика по пользователям за последние 12 месяцев
        Map<String, Integer> usersByMonth = getUserCountByMonth();
        
        // Статистика по курсам за последние 12 месяцев
        Map<String, Integer> coursesByMonth = getCourseCountByMonth();
        
        // Статистика по зачислениям за последние 12 месяцев
        Map<String, Integer> enrollmentsByMonth = getEnrollmentCountByMonth();
        
        // Статистика по категориям
        List<SystemAnalyticsResponse.CategoryStatsDTO> categoryStats = getCategoryStats();
        
        return SystemAnalyticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .studentCount(studentCount)
                .instructorCount(instructorCount)
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .totalEnrollments(totalEnrollments)
                .completedCourses(completedCourses)
                .systemWideCompletionRate(systemWideCompletionRate)
                .averageRating(averageRating)
                .usersByMonth(usersByMonth)
                .coursesByMonth(coursesByMonth)
                .enrollmentsByMonth(enrollmentsByMonth)
                .categoryStats(categoryStats)
                .build();
    }

    @Transactional(readOnly = true)
    public CourseAnalyticsResponse getCourseAnalytics(Long courseId, Long currentUserId) {
        // Проверка существования курса
        boolean courseExists = courseRepository.existsById(courseId);
        if (!courseExists) {
            throw new ResourceNotFoundException("Course not found");
        }
        
        // Проверка прав доступа - только инструктор курса или админ может просматривать аналитику курса
        boolean hasAccess = courseRepository.existsByIdAndInstructorId(courseId, currentUserId);
        if (!hasAccess) {
            throw new AccessDeniedException("You don't have permission to view analytics for this course");
        }
        
        // Название курса
        String courseTitle = courseRepository.findTitleById(courseId);
        
        // Агрегация данных о зачислениях
        Integer enrollmentCount = enrollmentRepository.countByCourseId(courseId).intValue();
        Integer completionCount = enrollmentRepository.countByCompletedAtIsNotNullAndCourseId(courseId).intValue();
        
        // Вычисление ставки завершения
        Double completionRate = enrollmentCount > 0 ? 
                (double) completionCount / enrollmentCount * 100 : 0.0;
        
        // Средний прогресс по курсу
        Double averageProgress = enrollmentRepository.getAverageProgressByCourseId(courseId);
        if (averageProgress == null) {
            averageProgress = 0.0;
        }
        
        // Средний рейтинг курса
        Double averageRating = reviewRepository.getAverageRatingByCourseId(courseId);
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // Количество отзывов
        Integer reviewCount = reviewRepository.countByCourseId(courseId).intValue();
        
        // Количество посетителей (примерное значение, в реальной системе нужна отдельная таблица для отслеживания)
        Long visitorCount = enrollmentCount * 3L; // пример, предполагаем в 3 раза больше просмотров, чем зачислений
        
        // Статистика по зачислениям за последние 12 месяцев
        Map<String, Long> enrollmentsByMonth = getEnrollmentsByMonthForCourse(courseId);
        
        // Статистика по прогрессу за последние 12 месяцев
        Map<String, Double> averageProgressByMonth = getAverageProgressByMonthForCourse(courseId);
        
        // Статистика по завершениям за последние 12 месяцев
        Map<String, Integer> completionsByMonth = getCompletionsByMonthForCourse(courseId);
        
        // Данные о взаимодействии с уроками (примерные, в реальной системе нужны отдельные таблицы для отслеживания)
        List<CourseAnalyticsResponse.LessonEngagementDTO> lessonEngagement = new ArrayList<>();
        // Здесь нужна логика для получения данных о взаимодействии с уроками
        
        return CourseAnalyticsResponse.builder()
                .courseId(courseId)
                .courseTitle(courseTitle)
                .enrollmentCount(enrollmentCount)
                .completionCount(completionCount)
                .completionRate(completionRate)
                .averageProgress(averageProgress)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .visitorCount(visitorCount)
                .enrollmentsByMonth(enrollmentsByMonth)
                .averageProgressByMonth(averageProgressByMonth)
                .completionsByMonth(completionsByMonth)
                .lessonEngagement(lessonEngagement)
                .build();
    }

    @Transactional(readOnly = true)
    public UserAnalyticsResponse getUserAnalytics(Long userId, Long currentUserId) {
        // Проверка существования пользователя
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new ResourceNotFoundException("User not found");
        }
        
        // Проверка прав доступа - только сам пользователь или админ может просматривать аналитику пользователя
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view analytics for this user");
        }
        
        // Имя пользователя
        String userName = userRepository.findFullNameById(userId);
        
        // Агрегация данных о зачислениях
        Integer enrolledCourses = enrollmentRepository.countByUserId(userId).intValue();
        Integer completedCourses = enrollmentRepository.countByCompletedAtIsNotNullAndUserId(userId).intValue();
        
        // Вычисление ставки завершения
        Double completionRate = enrolledCourses > 0 ? 
                (double) completedCourses / enrolledCourses * 100 : 0.0;
        
        // Средний прогресс по всем курсам
        Double averageProgress = enrollmentRepository.getAverageProgressByUserId(userId);
        if (averageProgress == null) {
            averageProgress = 0.0;
        }
        
        // Данные о тестах
        Integer testsTaken = testSubmissionRepository.countByUserId(userId).intValue();
        Double averageTestScore = testSubmissionRepository.getAverageScoreByUserId(userId);
        if (averageTestScore == null) {
            averageTestScore = 0.0;
        }
        
        // Количество полученных наград (примерное значение, в реальной системе нужна отдельная таблица)
        Integer earnedAchievements = 5; // пример
        
        // Статистика по прогрессу за последние 12 месяцев
        Map<String, Double> progressByMonth = getProgressByMonthForUser(userId);
        
        // Статистика по зачислениям за последние 12 месяцев
        Map<String, Integer> courseEnrollmentsByMonth = getCourseEnrollmentsByMonthForUser(userId);
        
        // Статистика по завершениям за последние 12 месяцев
        Map<String, Integer> courseCompletionsByMonth = getCourseCompletionsByMonthForUser(userId);
        
        // Данные о прогрессе по курсам
        List<UserAnalyticsResponse.CourseProgressDTO> courseProgress = getCourseProgressForUser(userId);
        
        return UserAnalyticsResponse.builder()
                .userId(userId)
                .userName(userName)
                .enrolledCourses(enrolledCourses)
                .completedCourses(completedCourses)
                .completionRate(completionRate)
                .averageProgress(averageProgress)
                .testsTaken(testsTaken)
                .averageTestScore(averageTestScore)
                .earnedAchievements(earnedAchievements)
                .progressByMonth(progressByMonth)
                .courseEnrollmentsByMonth(courseEnrollmentsByMonth)
                .courseCompletionsByMonth(courseCompletionsByMonth)
                .courseProgress(courseProgress)
                .build();
    }

    // Вспомогательные методы для получения статистики

    private Map<String, Integer> getUserCountByMonth() {
        Map<String, Integer> usersByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            usersByMonth.put(monthKey, 100 - i * 5);
        }
        
        return usersByMonth;
    }

    private Map<String, Integer> getCourseCountByMonth() {
        Map<String, Integer> coursesByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            coursesByMonth.put(monthKey, 50 - i * 2);
        }
        
        return coursesByMonth;
    }

    private Map<String, Integer> getEnrollmentCountByMonth() {
        Map<String, Integer> enrollmentsByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            enrollmentsByMonth.put(monthKey, 200 - i * 10);
        }
        
        return enrollmentsByMonth;
    }

    private List<SystemAnalyticsResponse.CategoryStatsDTO> getCategoryStats() {
        // В реальной системе нужно получать данные из БД с помощью специальных запросов
        // Здесь просто генерируем примерные данные
        return categoryRepository.findAll().stream()
                .map(category -> SystemAnalyticsResponse.CategoryStatsDTO.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .courseCount(10) // Пример
                        .enrollmentCount(100) // Пример
                        .averageRating(4.5) // Пример
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, Long> getEnrollmentsByMonthForCourse(Long courseId) {
        Map<String, Long> enrollmentsByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            enrollmentsByMonth.put(monthKey, 50L - i * 3);
        }
        
        return enrollmentsByMonth;
    }

    private Map<String, Double> getAverageProgressByMonthForCourse(Long courseId) {
        Map<String, Double> progressByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            progressByMonth.put(monthKey, 70.0 - i * 5);
        }
        
        return progressByMonth;
    }

    private Map<String, Integer> getCompletionsByMonthForCourse(Long courseId) {
        Map<String, Integer> completionsByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            completionsByMonth.put(monthKey, 30 - i * 2);
        }
        
        return completionsByMonth;
    }

    private Map<String, Double> getProgressByMonthForUser(Long userId) {
        Map<String, Double> progressByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            progressByMonth.put(monthKey, 10.0 * (i + 1));
        }
        
        return progressByMonth;
    }

    private Map<String, Integer> getCourseEnrollmentsByMonthForUser(Long userId) {
        Map<String, Integer> enrollmentsByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            enrollmentsByMonth.put(monthKey, i < 6 ? 1 : 0);
        }
        
        return enrollmentsByMonth;
    }

    private Map<String, Integer> getCourseCompletionsByMonthForUser(Long userId) {
        Map<String, Integer> completionsByMonth = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 0; i < 12; i++) {
            ZonedDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);
            
            // В реальной системе нужно получать данные из БД
            // Здесь просто генерируем примерные данные
            completionsByMonth.put(monthKey, i < 3 ? 1 : 0);
        }
        
        return completionsByMonth;
    }

    private List<UserAnalyticsResponse.CourseProgressDTO> getCourseProgressForUser(Long userId) {
        // В реальной системе нужно получать данные из БД с помощью специальных запросов
        // Здесь просто генерируем примерные данные
        List<UserAnalyticsResponse.CourseProgressDTO> courseProgress = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            courseProgress.add(UserAnalyticsResponse.CourseProgressDTO.builder()
                    .courseId((long) i)
                    .courseTitle("Course " + i)
                    .progress(i * 20.0)
                    .isCompleted(i <= 2)
                    .lastActiveDate(30 - i * 5)
                    .totalTimeSpent(i * 60)
                    .build());
        }
        
        return courseProgress;
    }
}
