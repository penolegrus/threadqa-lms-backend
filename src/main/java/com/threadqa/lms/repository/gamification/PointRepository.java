package com.threadqa.lms.repository.gamification;

import com.threadqa.lms.model.gamification.Point;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findByUser(User user);

    Page<Point> findByUser(User user, Pageable pageable);

    List<Point> findByUserAndPointType(User user, Point.PointType pointType);

    Page<Point> findByUserAndPointType(User user, Point.PointType pointType, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Point p WHERE p.user.id = :userId")
    Integer getTotalPointsByUser(Long userId);

    @Query("SELECT SUM(p.amount) FROM Point p WHERE p.user.id = :userId AND p.pointType = :pointType")
    Integer getTotalPointsByUserAndType(Long userId, Point.PointType pointType);

    @Query("SELECT SUM(p.amount) FROM Point p WHERE p.user.id = :userId AND p.createdAt >= :startDate")
    Integer getTotalPointsByUserSince(Long userId, ZonedDateTime startDate);

    @Query("SELECT p.user.id, SUM(p.amount) as total FROM Point p GROUP BY p.user.id ORDER BY total DESC")
    List<Object[]> getUsersWithTotalPoints();

    @Query("SELECT p.user.id, SUM(p.amount) as total FROM Point p WHERE p.createdAt >= :startDate GROUP BY p.user.id ORDER BY total DESC")
    List<Object[]> getUsersWithTotalPointsSince(ZonedDateTime startDate);
}
