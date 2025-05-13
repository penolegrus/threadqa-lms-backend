package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.topic.TopicResponse;
import com.threadqa.lms.model.course.Topic;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    public TopicResponse toTopicResponse(Topic topic, Integer contentCount) {
        if (topic == null) {
            return null;
        }

        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .courseId(topic.getCourse().getId())
                .courseName(topic.getCourse().getTitle())
                .orderIndex(topic.getOrderIndex())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .contentCount(contentCount)
                .build();
    }
}
