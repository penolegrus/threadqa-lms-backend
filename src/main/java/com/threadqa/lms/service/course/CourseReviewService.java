package com.threadqa.lms.service.course;

import com.threadqa.lms.dto.course.CourseReviewRequest;
import com.threadqa.lms.dto.course.CourseReviewResponse;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.CourseReviewMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.course.CourseReview;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.course.CourseReviewRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
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
public class CourseReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseReviewMapper reviewMapper;
    private final NotificationService notificationService;

    @Transactional
    public CourseReviewResponse createReview(CourseReviewRequest request, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверка, записан ли пользователь на курс
        Optional<CourseEnrollment> enrollment = enrollmentRepository.findByUserAndCourse(user, course);
        if (enrollment.isEmpty()) {
            throw new BadRequestException("You must be enrolled in the course to leave a review");
        }

        // Проверка, не оставлял ли пользователь уже отзыв
        Optional<CourseReview> existingReview = reviewRepository.findByUserAndCourse(user, course);
        if (existingReview.isPresent()) {
            throw new BadRequestException("You have already reviewed this course");
        }

        CourseReview review = CourseReview.builder()
                .user(user)
                .course(course)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        CourseReview savedReview = reviewRepository.save(review);

        // Отправка уведомления инструктору курса
        notificationService.createNotification(
                course.getInstructor().getId(),
                "Новый отзыв о курсе",
                user.getFirstName() + " " + user.getLastName() + " оставил отзыв о вашем курсе \"" + course.getTitle() + "\"",
                "COURSE_REVIEW",
                "/courses/" + course.getId() + "/reviews"
        );

        return reviewMapper.toCourseReviewResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<CourseReviewResponse> getReviewsByCourse(Long courseId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Page<CourseReview> reviews = reviewRepository.findByCourse(course, pageable);

        return reviews.map(reviewMapper::toCourseReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<CourseReview> reviews = reviewRepository.findByUser(user, pageable);

        return reviews.map(reviewMapper::toCourseReviewResponse);
    }

    @Transactional(readOnly = true)
    public CourseReviewResponse getReview(Long reviewId) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        return reviewMapper.toCourseReviewResponse(review);
    }

    @Transactional
    public CourseReviewResponse updateReview(Long reviewId, CourseReviewRequest request, Long currentUserId) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Проверка прав доступа - только автор отзыва может его редактировать
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this review");
        }

        // Проверка, что обновляемый отзыв относится к тому же курсу
        if (!review.getCourse().getId().equals(request.getCourseId())) {
            throw new BadRequestException("Course ID mismatch");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUpdatedAt(ZonedDateTime.now());

        CourseReview updatedReview = reviewRepository.save(review);

        return reviewMapper.toCourseReviewResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Проверка прав доступа - только автор отзыва или инструктор курса может его удалить
        if (!review.getUser().getId().equals(currentUserId) && 
            !review.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingByCourse(Long courseId) {
        return reviewRepository.getAverageRatingByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public Long getReviewCountByCourse(Long courseId) {
        return reviewRepository.countByCourseId(courseId);
    }
}
