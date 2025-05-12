package com.lms.mapper;

import com.lms.dto.topic.TopicResponse;
import com.lms.model.Topic;
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