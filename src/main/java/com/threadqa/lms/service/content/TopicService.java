package com.threadqa.lms.service.content;

import com.threadqa.lms.dto.topic.TopicRequest;
import com.threadqa.lms.dto.topic.TopicResponse;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.TopicMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.repository.assessment.TestRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.course.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final CourseRepository courseRepository;
    private final TestRepository testRepository;
    private final TopicMapper topicMapper;

    @Transactional(readOnly = true)
    public Page<TopicResponse> getAllTopics(Pageable pageable) {
        Page<Topic> topics = topicRepository.findAll(pageable);

        return topics.map(topic -> {
            Integer contentCount = countTopicContent(topic);
            return topicMapper.toTopicResponse(topic, contentCount);
        });
    }

    @Transactional(readOnly = true)
    public TopicResponse getTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Integer contentCount = countTopicContent(topic);

        return topicMapper.toTopicResponse(topic, contentCount);
    }

    @Transactional(readOnly = true)
    public Page<TopicResponse> getTopicsByCourse(Long courseId, Pageable pageable) {
        Page<Topic> topics = topicRepository.findByCourseId(courseId, pageable);

        return topics.map(topic -> {
            Integer contentCount = countTopicContent(topic);
            return topicMapper.toTopicResponse(topic, contentCount);
        });
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request, Long currentUserId) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Проверка прав доступа - только инструктор курса или админ может создавать темы
        if (!course.getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to create topics for this course");
        }

        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrderIndex = topicRepository.findMaxOrderIndexByCourseId(course.getId());
            orderIndex = (maxOrderIndex == null) ? 0 : maxOrderIndex + 1;
        }

        Topic topic = Topic.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .course(course)
                .orderIndex(orderIndex)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Topic savedTopic = topicRepository.save(topic);

        Integer contentCount = 0; // Новая тема еще не имеет контента

        return topicMapper.toTopicResponse(savedTopic, contentCount);
    }

    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicRequest request, Long currentUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Проверка прав доступа - только инструктор курса или админ может обновлять темы
        if (!topic.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this topic");
        }

        // Проверка, изменился ли курс
        if (!topic.getCourse().getId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

            // Проверка прав доступа для нового курса
            if (!newCourse.getInstructor().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't have permission to move this topic to the specified course");
            }

            topic.setCourse(newCourse);
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());

        if (request.getOrderIndex() != null) {
            topic.setOrderIndex(request.getOrderIndex());
        }

        topic.setUpdatedAt(ZonedDateTime.now());

        Topic updatedTopic = topicRepository.save(topic);

        Integer contentCount = countTopicContent(updatedTopic);

        return topicMapper.toTopicResponse(updatedTopic, contentCount);
    }

    @Transactional
    public void deleteTopic(Long topicId, Long currentUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Проверка прав доступа - только инструктор курса или админ может удалять темы
        if (!topic.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this topic");
        }

        // Проверка наличия связанного контента
        Long testCount = testRepository.countByTopicId(topicId);

        if (testCount > 0) {
            throw new IllegalStateException("Cannot delete topic with associated content");
        }

        topicRepository.delete(topic);
    }

    @Transactional
    public TopicResponse updateTopicOrder(Long topicId, Integer orderIndex, Long currentUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Проверка прав доступа - только инструктор курса или админ может изменять порядок тем
        if (!topic.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this topic");
        }

        topic.setOrderIndex(orderIndex);
        topic.setUpdatedAt(ZonedDateTime.now());

        Topic updatedTopic = topicRepository.save(topic);

        Integer contentCount = countTopicContent(updatedTopic);

        return topicMapper.toTopicResponse(updatedTopic, contentCount);
    }

    private Integer countTopicContent(Topic topic) {
        // Подсчет количества тестов в теме
        Long testCount = testRepository.countByTopicId(topic.getId());
        
        // В реальной системе здесь также может быть подсчет других типов контента
        // (видео, статьи, задания и т.д.)
        
        return testCount != null ? testCount.intValue() : 0;
    }
}
