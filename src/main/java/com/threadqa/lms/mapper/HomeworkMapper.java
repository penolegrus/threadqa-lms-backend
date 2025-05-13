package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.homework.*;
import com.threadqa.lms.model.homework.Homework;
import com.threadqa.lms.model.homework.HomeworkChatMessage;
import com.threadqa.lms.model.homework.HomeworkRequirement;
import com.threadqa.lms.model.homework.HomeworkSubmission;
import com.threadqa.lms.repository.homework.HomeworkChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HomeworkMapper {

    private final HomeworkChatMessageRepository chatMessageRepository;

    public HomeworkResponse toHomeworkResponse(
            Homework homework,
            Integer submissionCount,
            Integer completedCount,
            Double averageScore,
            Boolean isSubmitted,
            HomeworkSubmissionResponse userSubmission,
            List<HomeworkRequirementResponse> requirements) {

        if (homework == null) {
            return null;
        }

        return HomeworkResponse.builder()
                .id(homework.getId())
                .title(homework.getTitle())
                .description(homework.getDescription())
                .topicId(homework.getTopic().getId())
                .topicTitle(homework.getTopic().getTitle())
                .courseId(homework.getTopic().getCourse().getId())
                .courseTitle(homework.getTopic().getCourse().getTitle())
                .maxScore(homework.getMaxScore())
                .dueDate(homework.getDueDate())
                .isPublished(homework.getIsPublished())
                .createdAt(homework.getCreatedAt())
                .updatedAt(homework.getUpdatedAt())
                .publishedAt(homework.getPublishedAt())
                .submissionCount(submissionCount)
                .completedCount(completedCount)
                .averageScore(averageScore)
                .isSubmitted(isSubmitted)
                .userSubmission(userSubmission)
                .requirements(requirements)
                .build();
    }

    public HomeworkRequirementResponse toHomeworkRequirementResponse(HomeworkRequirement requirement) {
        if (requirement == null) {
            return null;
        }

        return HomeworkRequirementResponse.builder()
                .id(requirement.getId())
                .homeworkId(requirement.getHomework().getId())
                .description(requirement.getDescription())
                .orderIndex(requirement.getOrderIndex())
                .points(requirement.getPoints())
                .build();
    }

    public HomeworkSubmissionResponse toHomeworkSubmissionResponse(HomeworkSubmission submission, Long currentUserId) {
        if (submission == null) {
            return null;
        }

        // Преобразование строки с URL файлов в список
        List<String> fileUrls = Collections.emptyList();
        if (submission.getFileUrls() != null && !submission.getFileUrls().isEmpty()) {
            fileUrls = Arrays.asList(submission.getFileUrls().split(","));
        }

        // Подсчет непрочитанных сообщений
        Long unreadCount = 0L;
        if (currentUserId != null) {
            unreadCount = chatMessageRepository.countUnreadBySubmissionIdAndUserId(submission.getId(), currentUserId);
        }

        return HomeworkSubmissionResponse.builder()
                .id(submission.getId())
                .homeworkId(submission.getHomework().getId())
                .homeworkTitle(submission.getHomework().getTitle())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getFirstName() + " " + submission.getUser().getLastName())
                .content(submission.getContent())
                .fileUrls(fileUrls)
                .submittedAt(submission.getSubmittedAt())
                .reviewedAt(submission.getReviewedAt())
                .reviewerId(submission.getReviewer() != null ? submission.getReviewer().getId() : null)
                .reviewerName(submission.getReviewer() != null ? 
                        submission.getReviewer().getFirstName() + " " + submission.getReviewer().getLastName() : null)
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .status(submission.getStatus())
                .unreadMessageCount(unreadCount.intValue())
                .build();
    }

    public HomeworkChatMessageResponse toHomeworkChatMessageResponse(HomeworkChatMessage message) {
        if (message == null) {
            return null;
        }

        // Преобразование строки с URL файлов в список
        List<String> fileUrls = Collections.emptyList();
        if (message.getFileUrls() != null && !message.getFileUrls().isEmpty()) {
            fileUrls = Arrays.asList(message.getFileUrls().split(","));
        }

        // Определение роли отправителя
        String senderRole = message.getSender().getRoles().stream()
                .findFirst()
                .map(role -> role.getName().replace("ROLE_", ""))
                .orElse("USER");

        return HomeworkChatMessageResponse.builder()
                .id(message.getId())
                .submissionId(message.getSubmission().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .senderRole(senderRole)
                .content(message.getContent())
                .fileUrls(fileUrls)
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .build();
    }
}
