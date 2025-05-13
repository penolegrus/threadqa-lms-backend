package com.threadqa.lms.service.gamification;

import com.threadqa.lms.dto.gamification.*;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.GamificationMapper;
import com.threadqa.lms.model.gamification.*;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.gamification.*;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final PointRepository pointRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final LevelRepository levelRepository;
    private final UserLevelRepository userLevelRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final StreakRepository streakRepository;
    private final UserRepository userRepository;
    private final GamificationMapper gamificationMapper;
    private final NotificationService notificationService;

    @Transactional
    public PointResponse awardPoints(PointRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Point point = Point.builder()
                .user(user)
                .amount(request.getAmount())
                .pointType(request.getPointType())
                .description(request.getDescription())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .createdAt(ZonedDateTime.now())
                .build();

        Point savedPoint = pointRepository.save(point);

        // Обновляем уровень пользователя после начисления очков
        updateUserLevel(user);

        // Проверяем, заслужил ли пользователь новые значки
        checkAndAwardBadges(user);

        return gamificationMapper.toPointResponse(savedPoint);
    }

    @Transactional(readOnly = true)
    public Integer getTotalPoints(Long userId) {
        Integer totalPoints = pointRepository.getTotalPointsByUser(userId);
        return totalPoints != null ? totalPoints : 0;
    }

    @Transactional(readOnly = true)
    public Page<PointResponse> getUserPoints(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Point> points = pointRepository.findByUser(user, pageable);
        return points.map(gamificationMapper::toPointResponse);
    }

    @Transactional
    public BadgeResponse createBadge(BadgeRequest request) {
        Badge badge = Badge.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .badgeType(request.getBadgeType())
                .threshold(request.getThreshold())
                .pointsReward(request.getPointsReward())
                .isActive(request.getIsActive())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Badge savedBadge = badgeRepository.save(badge);
        return gamificationMapper.toBadgeResponse(savedBadge);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getAllBadges() {
        List<Badge> badges = badgeRepository.findAll();
        return badges.stream()
                .map(gamificationMapper::toBadgeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getActiveBadges() {
        List<Badge> badges = badgeRepository.findByIsActiveTrue();
        return badges.stream()
                .map(gamificationMapper::toBadgeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserBadgeResponse> getUserBadges(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserBadge> userBadges = userBadgeRepository.findByUser(user, pageable);
        return userBadges.map(gamificationMapper::toUserBadgeResponse);
    }

    @Transactional
    public LevelResponse createLevel(LevelRequest request) {
        Level level = Level.builder()
                .name(request.getName())
                .levelNumber(request.getLevelNumber())
                .pointsRequired(request.getPointsRequired())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .benefits(request.getBenefits())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Level savedLevel = levelRepository.save(level);
        return gamificationMapper.toLevelResponse(savedLevel);
    }

    @Transactional(readOnly = true)
    public List<LevelResponse> getAllLevels() {
        List<Level> levels = levelRepository.findAll();
        return levels.stream()
                .map(gamificationMapper::toLevelResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserLevelResponse getUserLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<UserLevel> userLevelOpt = userLevelRepository.findByUser(user);
        
        if (userLevelOpt.isPresent()) {
            return gamificationMapper.toUserLevelResponse(userLevelOpt.get());
        } else {
            // Если у пользователя еще нет уровня, создаем начальный уровень
            return updateUserLevel(user);
        }
    }

    @Transactional
    public LeaderboardResponse createLeaderboard(LeaderboardRequest request) {
        Leaderboard leaderboard = Leaderboard.builder()
                .name(request.getName())
                .description(request.getDescription())
                .leaderboardType(request.getLeaderboardType())
                .timePeriod(request.getTimePeriod())
                .isActive(request.getIsActive())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Leaderboard savedLeaderboard = leaderboardRepository.save(leaderboard);
        return gamificationMapper.toLeaderboardResponse(savedLeaderboard);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getActiveLeaderboards() {
        List<Leaderboard> leaderboards = leaderboardRepository.findByIsActiveTrue();
        return leaderboards.stream()
                .map(gamificationMapper::toLeaderboardResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponse> getLeaderboardEntries(Long leaderboardId, Pageable pageable) {
        Leaderboard leaderboard = leaderboardRepository.findById(leaderboardId)
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard not found"));

        Page<LeaderboardEntry> entries = leaderboardEntryRepository.findByLeaderboardOrderByRankAsc(leaderboard, pageable);
        return entries.map(gamificationMapper::toLeaderboardEntryResponse);
    }

    @Transactional(readOnly = true)
    public LeaderboardEntryResponse getUserLeaderboardPosition(Long leaderboardId, Long userId) {
        Optional<LeaderboardEntry> entryOpt = leaderboardEntryRepository.findByLeaderboardIdAndUserId(leaderboardId, userId);
        return entryOpt.map(gamificationMapper::toLeaderboardEntryResponse).orElse(null);
    }

    @Transactional
    public StreakResponse updateLoginStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Streak> streakOpt = streakRepository.findByUserAndStreakType(user, Streak.StreakType.LOGIN);
        
        Streak streak;
        ZonedDateTime now = ZonedDateTime.now();
        
        if (streakOpt.isPresent()) {
            streak = streakOpt.get();
            ZonedDateTime lastLogin = streak.getLastActivityDate();
            
            // Проверяем, был ли последний вход вчера или сегодня
            long daysBetween = ChronoUnit.DAYS.between(lastLogin.toLocalDate(), now.toLocalDate());
            
            if (daysBetween == 1) {
                // Последний вход был вчера, увеличиваем серию
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
                
                // Обновляем самую длинную серию, если текущая стала длиннее
                if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                    streak.setLongestStreak(streak.getCurrentStreak());
                }
                
                // Начисляем очки за серию входов
                awardStreakPoints(user, streak.getCurrentStreak());
                
                // Проверяем, заслужил ли пользователь значок за серию входов
                checkLoginStreakBadges(user, streak.getCurrentStreak());
            } else if (daysBetween > 1) {
                // Серия прервана, начинаем новую
                streak.setCurrentStreak(1);
            } else if (daysBetween == 0) {
                // Пользователь уже входил сегодня, ничего не меняем
                return gamificationMapper.toStreakResponse(streak);
            }
        } else {
            // Создаем новую запись о серии входов
            streak = Streak.builder()
                    .user(user)
                    .currentStreak(1)
                    .longestStreak(1)
                    .streakType(Streak.StreakType.LOGIN)
                    .createdAt(now)
                    .build();
        }
        
        streak.setLastActivityDate(now);
        streak.setUpdatedAt(now);
        
        Streak savedStreak = streakRepository.save(streak);
        return gamificationMapper.toStreakResponse(savedStreak);
    }

    @Transactional(readOnly = true)
    public List<StreakResponse> getUserStreaks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Streak> streaks = streakRepository.findByUser(user);
        return streaks.stream()
                .map(gamificationMapper::toStreakResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GamificationSummaryResponse getUserGamificationSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получаем общее количество очков
        Integer totalPoints = getTotalPoints(userId);
        
        // Получаем уровень пользователя
        Optional<UserLevel> userLevelOpt = userLevelRepository.findByUser(user);
        UserLevel userLevel = userLevelOpt.orElse(null);
        
        // Получаем последние полученные значки (до 5)
        List<UserBadge> recentBadges = userBadgeRepository.findByUser(user).stream()
                .sorted((b1, b2) -> b2.getAwardedAt().compareTo(b1.getAwardedAt()))
                .limit(5)
                .collect(Collectors.toList());
        
        // Получаем серию входов
        Optional<Streak> loginStreakOpt = streakRepository.findByUserAndStreakType(user, Streak.StreakType.LOGIN);
        Streak loginStreak = loginStreakOpt.orElse(null);
        
        // Получаем позиции в таблицах лидеров
        List<Leaderboard> activeLeaderboards = leaderboardRepository.findByIsActiveTrue();
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        
        for (Leaderboard leaderboard : activeLeaderboards) {
            Optional<LeaderboardEntry> entryOpt = leaderboardEntryRepository.findByLeaderboardAndUser(leaderboard, user);
            entryOpt.ifPresent(leaderboardEntries::add);
        }
        
        return gamificationMapper.toGamificationSummaryResponse(
                userId, user.getFirstName() + " " + user.getLastName(),
                totalPoints, userLevel, recentBadges, loginStreak, leaderboardEntries);
    }

    // Вспомогательные методы

    @Transactional
    protected UserLevelResponse updateUserLevel(User user) {
        Integer totalPoints = getTotalPoints(user.getId());
        
        // Находим текущий уровень пользователя на основе его очков
        Level currentLevel = levelRepository.findHighestLevelForPoints(totalPoints);
        
        if (currentLevel == null) {
            // Если нет подходящего уровня, используем уровень 1
            currentLevel = levelRepository.findByLevelNumber(1)
                    .orElseThrow(() -> new ResourceNotFoundException("Level 1 not found"));
        }
        
        // Находим следующий уровень
        Level nextLevel = levelRepository.findNextLevelForPoints(totalPoints);
        Integer pointsToNextLevel = 0;
        
        if (nextLevel != null) {
            pointsToNextLevel = nextLevel.getPointsRequired() - totalPoints;
        }
        
        // Проверяем, есть ли уже запись об уровне пользователя
        Optional<UserLevel> userLevelOpt = userLevelRepository.findByUser(user);
        
        UserLevel userLevel;
        ZonedDateTime now = ZonedDateTime.now();
        
        if (userLevelOpt.isPresent()) {
            userLevel = userLevelOpt.get();
            
            // Проверяем, изменился ли уровень
            if (!userLevel.getLevel().getId().equals(currentLevel.getId())) {
                // Уровень изменился, обновляем запись
                userLevel.setLevel(currentLevel);
                userLevel.setAchievedAt(now);
                
                // Отправляем уведомление о новом уровне
                notificationService.sendLevelUpNotification(user, currentLevel.getLevelNumber(), currentLevel.getName());
            }
            
            userLevel.setCurrentPoints(totalPoints);
            userLevel.setPointsToNextLevel(pointsToNextLevel);
            userLevel.setUpdatedAt(now);
        } else {
            // Создаем новую запись об уровне пользователя
            userLevel = UserLevel.builder()
                    .user(user)
                    .level(currentLevel)
                    .currentPoints(totalPoints)
                    .pointsToNextLevel(pointsToNextLevel)
                    .achievedAt(now)
                    .updatedAt(now)
                    .build();
        }
        
        UserLevel savedUserLevel = userLevelRepository.save(userLevel);
        return gamificationMapper.toUserLevelResponse(savedUserLevel);
    }

    @Transactional
    protected void checkAndAwardBadges(User user) {
        // Получаем общее количество очков пользователя
        Integer totalPoints = getTotalPoints(user.getId());
        
        // Получаем все активные значки
        List<Badge> activeBadges = badgeRepository.findByIsActiveTrue();
        
        for (Badge badge : activeBadges) {
            // Проверяем, есть ли уже у пользователя этот значок
            Optional<UserBadge> existingBadge = userBadgeRepository.findByUserAndBadge(user, badge);
            
            if (existingBadge.isPresent()) {
                continue; // Пользователь уже имеет этот значок
            }
            
            // Проверяем, заслужил ли пользователь этот значок
            boolean badgeEarned = false;
            
            switch (badge.getBadgeType()) {
                case COURSE_COMPLETION:
                    // Логика для значков за завершение курсов
                    // Здесь должен быть код для проверки количества завершенных курсов
                    break;
                case LOGIN_STREAK:
                    // Логика для значков за серию входов
                    // Проверяется в отдельном методе checkLoginStreakBadges
                    break;
                case ACHIEVEMENT_COLLECTOR:
                    // Логика для значков за коллекционирование достижений
                    Long badgeCount = userBadgeRepository.countBadgesByUser(user.getId());
                    if (badgeCount >= badge.getThreshold()) {
                        badgeEarned = true;
                    }
                    break;
                default:
                    // Для других типов значков можно использовать общую логику на основе очков
                    if (totalPoints >= badge.getThreshold()) {
                        badgeEarned = true;
                    }
                    break;
            }
            
            if (badgeEarned) {
                awardBadge(user, badge);
            }
        }
    }

    @Transactional
    protected void awardBadge(User user, Badge badge) {
        ZonedDateTime now = ZonedDateTime.now();
        
        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .awardedAt(now)
                .isDisplayed(true)
                .notificationSent(false)
                .build();
        
        userBadgeRepository.save(userBadge);
        
        // Если за значок предусмотрено вознаграждение в виде очков, начисляем их
        if (badge.getPointsReward() != null && badge.getPointsReward() > 0) {
            PointRequest pointRequest = PointRequest.builder()
                    .amount(badge.getPointsReward())
                    .pointType(Point.PointType.ACHIEVEMENT_UNLOCK)
                    .description("Награда за получение значка: " + badge.getName())
                    .entityType("Badge")
                    .entityId(badge.getId())
                    .build();
            
            awardPoints(pointRequest, user.getId());
        }
    }

    @Transactional
    protected void awardStreakPoints(User user, Integer streakDays) {
        // Начисляем очки за серию входов
        // Чем дольше серия, тем больше очков
        int pointsToAward = 5; // Базовое количество очков
        
        // Бонусы за длительные серии
        if (streakDays >= 30) {
            pointsToAward = 25;
        } else if (streakDays >= 14) {
            pointsToAward = 15;
        } else if (streakDays >= 7) {
            pointsToAward = 10;
        }
        
        PointRequest pointRequest = PointRequest.builder()
                .amount(pointsToAward)
                .pointType(Point.PointType.STREAK_BONUS)
                .description("Бонус за серию входов: " + streakDays + " дней подряд")
                .build();
        
        awardPoints(pointRequest, user.getId());
    }

    @Transactional
    protected void checkLoginStreakBadges(User user, Integer streakDays) {
        // Проверяем, заслужил ли пользователь значки за серию входов
        List<Badge> streakBadges = badgeRepository.findByBadgeTypeAndIsActiveTrue(Badge.BadgeType.LOGIN_STREAK);
        
        for (Badge badge : streakBadges) {
            // Проверяем, есть ли уже у пользователя этот значок
            Optional<UserBadge> existingBadge = userBadgeRepository.findByUserAndBadge(user, badge);
            
            if (existingBadge.isPresent()) {
                continue; // Пользователь уже имеет этот значок
            }
            
            // Проверяем, достиг ли пользователь порогового значения для значка
            if (streakDays >= badge.getThreshold()) {
                awardBadge(user, badge);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Запускается каждый день в полночь
    @Transactional
    public void updateLeaderboards() {
        ZonedDateTime now = ZonedDateTime.now();
        
        // Обновляем все активные таблицы лидеров
        List<Leaderboard> activeLeaderboards = leaderboardRepository.findByIsActiveTrue();
        
        for (Leaderboard leaderboard : activeLeaderboards) {
            // Определяем период для таблицы лидеров
            ZonedDateTime periodStart;
            ZonedDateTime periodEnd;
            
            switch (leaderboard.getTimePeriod()) {
                case DAILY:
                    periodStart = now.truncatedTo(ChronoUnit.DAYS);
                    periodEnd = periodStart.plusDays(1);
                    break;
                case WEEKLY:
                    periodStart = now.truncatedTo(ChronoUnit.DAYS).with(java.time.DayOfWeek.MONDAY);
                    periodEnd = periodStart.plusWeeks(1);
                    break;
                case MONTHLY:
                    periodStart = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                    periodEnd = periodStart.plusMonths(1);
                    break;
                case ALL_TIME:
                default:
                    periodStart = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, now.getZone());
                    periodEnd = now.plusYears(100);
                    break;
            }
            
            // Получаем данные для таблицы лидеров
            List<Object[]> leaderboardData;
            
            switch (leaderboard.getLeaderboardType()) {
                case POINTS:
                    leaderboardData = pointRepository.getUsersWithTotalPointsSince(periodStart);
                    break;
                // Здесь должны быть другие типы таблиц лидеров
                default:
                    leaderboardData = pointRepository.getUsersWithTotalPointsSince(periodStart);
                    break;
            }
            
            // Обновляем записи в таблице лидеров
            int rank = 1;
            for (Object[] data : leaderboardData) {
                Long userId = (Long) data[0];
                Integer score = ((Number) data[1]).intValue();
                
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                // Проверяем, есть ли уже запись для этого пользователя
                Optional<LeaderboardEntry> entryOpt = leaderboardEntryRepository.findByLeaderboardAndUser(leaderboard, user);
                
                LeaderboardEntry entry;
                if (entryOpt.isPresent()) {
                    entry = entryOpt.get();
                    entry.setScore(score);
                    entry.setRank(rank);
                    entry.setPeriodStart(periodStart);
                    entry.setPeriodEnd(periodEnd);
                    entry.setUpdatedAt(now);
                } else {
                    entry = LeaderboardEntry.builder()
                            .leaderboard(leaderboard)
                            .user(user)
                            .score(score)
                            .rank(rank)
                            .periodStart(periodStart)
                            .periodEnd(periodEnd)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                }
                
                leaderboardEntryRepository.save(entry);
                rank++;
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Запускается каждый день в полночь
    @Transactional
    public void resetExpiredStreaks() {
        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        
        // Находим все серии, которые не обновлялись вчера или ранее
        List<Streak> streaksToReset = streakRepository.findStreaksToReset(yesterday);
        
        for (Streak streak : streaksToReset) {
            streak.setCurrentStreak(0);
            streak.setUpdatedAt(ZonedDateTime.now());
            streakRepository.save(streak);
        }
    }

    @Scheduled(cron = "0 0 * * * ?") // Запускается каждый час
    @Transactional
    public void sendBadgeNotifications() {
        // Находим все значки, для которых еще не отправлены уведомления
        List<UserBadge> badgesToNotify = userBadgeRepository.findByNotificationSentFalse();
        
        for (UserBadge userBadge : badgesToNotify) {
            // Отправляем уведомление о получении значка
            notificationService.sendBadgeNotification(
                    userBadge.getUser(),
                    userBadge.getBadge().getName(),
                    userBadge.getBadge().getDescription());
            
            // Отмечаем, что уведомление отправлено
            userBadge.setNotificationSent(true);
            userBadgeRepository.save(userBadge);
        }
    }
}
