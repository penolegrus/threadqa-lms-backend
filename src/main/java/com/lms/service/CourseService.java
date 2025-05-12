package com.lms.service;

import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.course.PublicCourseResponse;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.CourseMapper;
import com.lms.model.Course;
import com.lms.model.CourseEnrollment;
import com.lms.model.User;
import com.lms.repository.CourseEnrollmentRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.CourseReviewRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository reviewRepository;
    private final CourseMapper courseMapper;

    public Page<CourseResponse> getAllCourses(String search, String category, String level, Pageable pageable) {
        Page<Course> courses;

        if (search != null && !search.isEmpty() && category != null && !category.isEmpty() && level != null && !level.isEmpty()) {
            courses = courseRepository.findByTitleContainingAndCategoryAndLevel(
                    search, category, Course.CourseLevel.valueOf(level), pageable);
        } else if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            courses = courseRepository.findByTitleContainingAndCategory(search, category, pageable);
        } else if (search != null && !search.isEmpty() && level != null && !level.isEmpty()) {
            courses = courseRepository.findByTitleContainingAndLevel(search, Course.CourseLevel.valueOf(level), pageable);
        } else if (category != null && !category.isEmpty() && level != null && !level.isEmpty()) {
            courses = courseRepository.findByCategoryAndLevel(category, Course.CourseLevel.valueOf(level), pageable);
        } else if (search != null && !search.isEmpty()) {
            courses = courseRepository.findByTitleContaining(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            courses = courseRepository.findByCategory(category, pageable);
        } else if (level != null && !level.isEmpty()) {
            courses = courseRepository.findByLevel(Course.CourseLevel.valueOf(level), pageable);
        } else {
            courses = courseRepository.findAll(pageable);
        }

        return courses.map(courseMapper::toCourseResponse);
    }

    public Page<PublicCourseResponse> getPublicCourses(String search, String category, String level, Pageable pageable) {
        Page<Course> courses = getAllCourses(search, category, level, pageable).map(courseMapper::toCourse);
        return courses.map(courseMapper::toPublicCourseResponse);
    }

    public List<Map<String, Object>> getCourseCategories() {
        return courseRepository.findCourseCategories();
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        return courseMapper.toCourseResponse(course);
    }

    public PublicCourseResponse getPublicCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        return courseMapper.toPublicCourseResponse(course);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest courseRequest) {
        Course course = new Course();
        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());
        course.setShortDescription(courseRequest.getShortDescription());
        course.setPrice(courseRequest.getPrice());
        course.setCategory(courseRequest.getCategory());
        course.setLevel(Course.CourseLevel.valueOf(courseRequest.getLevel()));
        course.setImageUrl(courseRequest.getImageUrl());
        course.setCoverImageUrl(courseRequest.getCoverImageUrl());
        course.setDuration(courseRequest.getDuration());
        course.setStatus(Course.CourseStatus.valueOf(courseRequest.getStatus()));

        if (courseRequest.getSkills() != null) {
            course.setSkills(new HashSet<>(courseRequest.getSkills()));
        }

        Set<User> instructors = courseRequest.getInstructorIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + id)))
                .collect(Collectors.toSet());

        course.setInstructors(instructors);

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toCourseResponse(savedCourse);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest courseRequest) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());
        course.setShortDescription(courseRequest.getShortDescription());
        course.setPrice(courseRequest.getPrice());
        course.setCategory(courseRequest.getCategory());
        course.setLevel(Course.CourseLevel.valueOf(courseRequest.getLevel()));
        course.setImageUrl(courseRequest.getImageUrl());
        course.setCoverImageUrl(courseRequest.getCoverImageUrl());
        course.setDuration(courseRequest.getDuration());
        course.setStatus(Course.CourseStatus.valueOf(courseRequest.getStatus()));

        if (courseRequest.getSkills() != null) {
            course.setSkills(new HashSet<>(courseRequest.getSkills()));
        }

        Set<User> instructors = courseRequest.getInstructorIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + id)))
                .collect(Collectors.toSet());

        course.setInstructors(instructors);

        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toCourseResponse(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }

        courseRepository.deleteById(id);
    }

    @Transactional
    public void enrollUserInCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("User is already enrolled in this course");
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(ZonedDateTime.now());

        enrollmentRepository.save(enrollment);
    }
}