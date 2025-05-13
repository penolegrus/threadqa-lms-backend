package com.threadqa.lms.service.course;

import com.threadqa.lms.dto.course.CourseRequest;
import com.threadqa.lms.dto.course.CourseResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.CourseMapper;
import com.threadqa.lms.model.course.Category;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.assessment.TestRepository;
import com.threadqa.lms.repository.course.CategoryRepository;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.course.CourseReviewRepository;
import com.threadqa.lms.repository.course.TopicRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository reviewRepository;
    private final TestRepository testRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(Pageable pageable, Long currentUserId) {
        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(course -> mapCourseToResponse(course, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getPublishedCourses(Pageable pageable, Long currentUserId) {
        Page<Course> courses = courseRepository.findByIsPublishedTrue(pageable);

        return courses.map(course -> mapCourseToResponse(course, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getFeaturedCourses(Pageable pageable, Long currentUserId) {
        Page<Course> courses = courseRepository.findByIsFeaturedTrue(pageable);

        return courses.map(course -> mapCourseToResponse(course, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> searchCourses(String keyword, Pageable pageable, Long currentUserId) {
        Page<Course> courses = courseRepository.searchCourses(keyword, pageable);

        return courses.map(course -> mapCourseToResponse(course, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByCategory(Long categoryId, Pageable pageable, Long currentUserId) {
        Page<Course> courses = courseRepository.findByCategoryId(categoryId, pageable);

        return courses.map(course -> mapCourseToResponse(course, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByInstructor(Long instructorId, Pageable pageable, Long currentUserId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Page<Course> courses = courseRepository.findByInstructor(instructor, pageable);

        return courses.map(course -> mapCourseToResponse(course, currentUserId));
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long courseId, Long currentUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        return mapCourseToResponse(course, currentUserId);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request, Long currentUserId) {
        User instructor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId)))
                    .collect(Collectors.toSet());
        }

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .coverImage(request.getCoverImage())
                .isPublished(request.getIsPublished())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .discountStartDate(request.getDiscountStartDate())
                .discountEndDate(request.getDiscountEndDate())
                .instructor(instructor)
                .categories(categories)
                .durationHours(request.getDurationHours())
                .level(request.getLevel())
                .language(request.getLanguage())
                .prerequisites(request.getPrerequisites())
                .learningObjectives(request.getLearningObjectives())
                .targetAudience(request.getTargetAudience())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        if (Boolean.TRUE.equals(request.getIsPublished())) {
            course.setPublishedAt(ZonedDateTime.now());
        }

        Course savedCourse = courseRepository.save(course);

        return mapCourseToResponse(savedCourse, currentUserId);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest request, Long currentUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверка прав доступа - только инструктор курса или админ может обновлять курс
        if (!course.getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this course");
        }

        // Обновление категорий
        if (request.getCategoryIds() != null) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId)))
                    .collect(Collectors.toSet());
            course.setCategories(categories);
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());

        if (request.getCoverImage() != null) {
            course.setCoverImage(request.getCoverImage());
        }

        // Обновление статуса публикации
        if (Boolean.TRUE.equals(request.getIsPublished()) && !Boolean.TRUE.equals(course.getIsPublished())) {
            course.setIsPublished(true);
            course.setPublishedAt(ZonedDateTime.now());
        } else if (Boolean.FALSE.equals(request.getIsPublished())) {
            course.setIsPublished(false);
        }

        if (request.getIsFeatured() != null) {
            course.setIsFeatured(request.getIsFeatured());
        }

        course.setPrice(request.getPrice());
        course.setDiscountPrice(request.getDiscountPrice());
        course.setDiscountStartDate(request.getDiscountStartDate());
        course.setDiscountEndDate(request.getDiscountEndDate());
        course.setDurationHours(request.getDurationHours());
        course.setLevel(request.getLevel());
        course.setLanguage(request.getLanguage());
        course.setPrerequisites(request.getPrerequisites());
        course.setLearningObjectives(request.getLearningObjectives());
        course.setTargetAudience(request.getTargetAudience());
        course.setUpdatedAt(ZonedDateTime.now());

        Course updatedCourse = courseRepository.save(course);

        return mapCourseToResponse(updatedCourse, currentUserId);
    }

    @Transactional
    public void deleteCourse(Long courseId, Long currentUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверка прав доступа - только инструктор курса или админ может удалять курс
        if (!course.getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this course");
        }

        // Проверка наличия зачисленных студентов
        Long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
        if (enrollmentCount > 0) {
            throw new IllegalStateException("Cannot delete course with enrolled students");
        }

        courseRepository.delete(course);
    }

    private CourseResponse mapCourseToResponse(Course course, Long currentUserId) {
        Integer topicCount = (int) topicRepository.findByCourse(course).size();
        Long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
        Double averageRating = reviewRepository.getAverageRatingByCourseId(course.getId());
        Long reviewCount = reviewRepository.countByCourseId(course.getId());

        Boolean isEnrolled = false;
        Boolean isCompleted = false;
        Double progress = 0.0;

        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                var enrollment = enrollmentRepository.findByUserAndCourse(currentUser, course).orElse(null);
                if (enrollment != null) {
                    isEnrolled = true;
                    isCompleted = enrollment.getCompletedAt() != null;
                    progress = enrollment.getProgress();
                }
            }
        }

        return courseMapper.toCourseResponse(
                course,
                topicCount,
                enrollmentCount != null ? enrollmentCount.intValue() : 0,
                averageRating != null ? averageRating : 0.0,
                reviewCount != null ? reviewCount.intValue() : 0,
                isEnrolled,
                isCompleted,
                progress
        );
    }
}