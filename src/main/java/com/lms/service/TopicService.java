package com.lms.service;

import com.lms.dto.topic.TopicRequest;
import com.lms.dto.topic.TopicResponse;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.TopicMapper;
import com.lms.model.Course;
import com.lms.model.Topic;
import com.lms.repository.CourseRepository;
import com.lms.repository.TestRepository;
import com.lms.repository.TopicRepository;
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
    public TopicResponse createTopic(TopicRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

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

        return topicMapper.toTopicResponse(savedTopic, 0);
    }

    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Check if course is changing
        if (!topic.getCourse().getId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            topic.setCourse(newCourse);

            // If course is changing, set order index to the end of the new course
            if (request.getOrderIndex() == null) {
                Integer maxOrderIndex = topicRepository.findMaxOrderIndexByCourseId(newCourse.getId());
                topic.setOrderIndex((maxOrderIndex == null) ? 0 : maxOrderIndex + 1);
            } else {
                topic.setOrderIndex(request.getOrderIndex());
            }
        } else if (request.getOrderIndex() != null) {
            // If course is not changing but order index is provided
            topic.setOrderIndex(request.getOrderIndex());
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setUpdatedAt(ZonedDateTime.now());

        Topic updatedTopic = topicRepository.save(topic);

        Integer contentCount = countTopicContent(updatedTopic);

        return topicMapper.toTopicResponse(updatedTopic, contentCount);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Check if topic has content
        Integer contentCount = countTopicContent(topic);
        if (contentCount > 0) {
            throw new IllegalStateException("Cannot delete topic with content. Remove all content first.");
        }

        topicRepository.delete(topic);
    }

    @Transactional
    public TopicResponse updateTopicOrder(Long topicId, Integer orderIndex) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        topic.setOrderIndex(orderIndex);
        topic.setUpdatedAt(ZonedDateTime.now());

        Topic updatedTopic = topicRepository.save(topic);

        Integer contentCount = countTopicContent(updatedTopic);

        return topicMapper.toTopicResponse(updatedTopic, contentCount);
    }

    private Integer countTopicContent(Topic topic) {
        // Count tests associated with this topic
        long testCount = testRepository.findByTopic(topic).size();

        // In a real implementation, you would also count other content types
        // like lectures, quizzes, etc.

        return (int) testCount;
    }
}