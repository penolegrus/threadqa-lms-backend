package com.threadqa.lms.repository.user;

import com.threadqa.lms.model.user.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Находит все активные сессии пользователя
     */
    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Находит все активные сессии пользователя с пагинацией
     */
    Page<UserSession> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    /**
     * Находит сессию по токену
     */
    Optional<UserSession> findByToken(String token);

    /**
     * Находит активную сессию по токену
     */
    Optional<UserSession> findByTokenAndIsActiveTrue(String token);

    /**
     * Подсчитывает количество активных сессий пользователя
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Деактивирует все сессии пользователя
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.expiredAt = CURRENT_TIMESTAMP WHERE s.userId = :userId AND s.isActive = true")
    void deactivateAllUserSessions(@Param("userId") Long userId);

    /**
     * Деактивирует все сессии пользователя, кроме указанной
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.expiredAt = CURRENT_TIMESTAMP WHERE s.userId = :userId AND s.id != :sessionId AND s.isActive = true")
    void deactivateAllUserSessionsExcept(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    /**
     * Находит самую старую активную сессию пользователя
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isActive = true ORDER BY s.createdAt ASC")
    List<UserSession> findOldestActiveSessionByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Находит все сессии, которые не были активны в течение указанного времени
     */
    List<UserSession> findByIsActiveTrueAndLastActivityAtBefore(LocalDateTime time);

    /**
     * Находит все сессии пользователя из указанного города
     */
    List<UserSession> findByUserIdAndCityAndIsActiveTrue(Long userId, String city);

    /**
     * Находит все сессии пользователя с указанного устройства
     */
    List<UserSession> findByUserIdAndDeviceTypeAndIsActiveTrue(Long userId, String deviceType);
}
