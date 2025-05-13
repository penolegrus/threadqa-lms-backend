package com.threadqa.lms.repository.progress;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.progress.UserProgress;
import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    List<UserProgress> findByUser(User user);

    Page<UserProgress> findByUser(User user, Pageable pageable);

    List<UserProgress> findByUserAndCourse(User user, Course course);

    Page<UserProgress> findByUserAndCourse(User user, Course course, Pageable pageable);

    List<UserProgress> findByUserAndTopic(User user, Topic topic);

    Page<UserProgress> findByUserAndTopic(User user, Topic topic, Pageable pageable);

    Optional<UserProgress> findByUserAndCourseAndTopicAndContentTypeAndContentId(
            User user, Course course, Topic topic, 
            UserProgress.ContentType contentType, Long contentId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.course.id = :courseId AND up.isCompleted = true")
    Long countCompletedItemsByCourseAndUser(Long courseId, Long userId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.topic.id = :topicId AND up.isCompleted = true")
    Long countCompletedItemsByTopicAndUser(Long topicId, Long userId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.course.id = :courseId")
    Long countTotalItemsByCourse(Long courseId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.topic.id = :topicId")
    Long countTotalItemsByTopic(Long topicId);

    @Query("SELECT AVG(up.progressPercentage) FROM UserProgress up WHERE up.user.id = :userId AND up.course.id = :courseId")
    Double getAverageProgressByCourseAndUser(Long courseId, Long userId);

    @Query("SELECT SUM(up.timeSpentSeconds) FROM UserProgress up WHERE up.user.id = :userId AND up.course.id = :courseId")
    Long getTotalTimeSpentByCourseAndUser(Long courseId, Long userId);

    @Query("SELECT SUM(up.timeSpentSeconds) FROM UserProgress up WHERE up.user.id = :userId")
    Long getTotalTimeSpentByUser(Long userId);
}
