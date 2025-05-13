package com.threadqa.lms.service.course;

import com.threadqa.lms.dto.course.CourseEnrollmentRequest;
import com.threadqa.lms.dto.course.CourseEnrollmentResponse;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.auth.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public CourseEnrollmentResponse enrollInCourse(CourseEnrollmentRequest request, Long currentUserId) {
        // Определение пользователя
        User user;
        if (request.getUserId() != null && !request.getUserId().equals(currentUserId)) {
            // Проверка прав доступа - только админ может записать другого пользователя
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } else {
            user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        // Получение курса
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверка, не записан ли пользователь уже на курс
        if (enrollmentRepository.findByUserAndCourse(user, course).isPresent()) {
            throw new BadRequestException("User is already enrolled in this course");
        }

        // Создание записи о зачислении
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .user(user)
                .course(course)
                .enrolledAt(ZonedDateTime.now())
                .progress(0.0)
                .isActive(true)
                .build();

        CourseEnrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Отправка письма с подтверждением
        emailService.sendCourseEnrollmentConfirmation(user, course.getTitle());

        return mapToResponse(savedEnrollment);
    }

    @Transactional(readOnly = true)
    public Page<CourseEnrollmentResponse> getUserEnrollments(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<CourseEnrollment> enrollments = enrollmentRepository.findByUser(user, pageable);

        return enrollments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseEnrollmentResponse> getCourseEnrollments(Long courseId, Pageable pageable, Long currentUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверка прав доступа - только инструктор курса или админ может видеть список зачисленных
        if (!course.getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view enrollments for this course");
        }

        Page<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course, pageable);

        return enrollments.map(this::mapToResponse);
    }

    @Transactional
    public CourseEnrollmentResponse updateProgress(Long enrollmentId, Double progress, Long currentUserId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Проверка прав доступа - только сам пользователь может обновить свой прогресс
        if (!enrollment.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this enrollment");
        }

        // Валидация прогресса
        if (progress < 0 || progress > 100) {
            throw new BadRequestException("Progress must be between 0 and 100");
        }

        enrollment.setProgress(progress);

        // Если прогресс 100%, автоматически отмечаем курс как завершенный
        if (progress >= 100.0 && enrollment.getCompletedAt() == null) {
            enrollment.setCompletedAt(ZonedDateTime.now());
        }

        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        return mapToResponse(updatedEnrollment);
    }

    @Transactional
    public CourseEnrollmentResponse markAsCompleted(Long enrollmentId, Long currentUserId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Проверка прав доступа - только сам пользователь или инструктор курса может отметить курс как завершенный
        if (!enrollment.getUser().getId().equals(currentUserId) && 
            !enrollment.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this enrollment");
        }

        enrollment.setCompletedAt(ZonedDateTime.now());
        enrollment.setProgress(100.0);

        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        return mapToResponse(updatedEnrollment);
    }

    @Transactional
    public void unenrollFromCourse(Long enrollmentId, Long currentUserId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Проверка прав доступа - только сам пользователь или инструктор курса может отменить запись
        if (!enrollment.getUser().getId().equals(currentUserId) && 
            !enrollment.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this enrollment");
        }

        enrollmentRepository.delete(enrollment);
    }

    private CourseEnrollmentResponse mapToResponse(CourseEnrollment enrollment) {
        return CourseEnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .userName(enrollment.getUser().getFirstName() + " " + enrollment.getUser().getLastName())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .progress(enrollment.getProgress())
                .isActive(enrollment.getIsActive())
                .build();
    }
}
