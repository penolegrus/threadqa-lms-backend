package com.lms.mapper;

import com.lms.dto.homework.*;
import com.lms.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HomeworkMapper {

    public HomeworkResponse toHomeworkResponse(Homework homework, List<HomeworkRequirementResponse> requirements, Integer submissionCount) {
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
                .dueDate(homework.getDueDate())
                .maxPoints(homework.getMaxPoints())
                .createdAt(homework.getCreatedAt())
                .updatedAt(homework.getUpdatedAt())
                .requirements(requirements)
                .submissionCount(submissionCount)
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
                .points(requirement.getPoints())
                .isRequired(requirement.getIsRequired())
                .build();
    }

    public HomeworkSubmissionResponse toHomeworkSubmissionResponse(
            HomeworkSubmission submission,
            List<String> fileUrls,
            List<HomeworkChatMessageResponse> recentMessages) {
        if (submission == null) {
            return null;
        }

        return HomeworkSubmissionResponse.builder()
                .id(submission.getId())
                .homeworkId(submission.getHomework().getId())
                .homeworkTitle(submission.getHomework().getTitle())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getFirstName() + " " + submission.getUser().getLastName())
                .submissionText(submission.getSubmissionText())
                .githubUrl(submission.getGithubUrl())
                .additionalNotes(submission.getAdditionalNotes())
                .fileUrls(fileUrls)
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .isGraded(submission.getIsGraded())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .recentMessages(recentMessages)
                .build();
    }

    public HomeworkChatMessageResponse toHomeworkChatMessageResponse(HomeworkChatMessage message) {
        if (message == null) {
            return null;
        }

        String userRole = "STUDENT";
        if (message.getUser().getRoles() != null) {
            for (Role role : message.getUser().getRoles()) {
                if (role.getName().equals("ROLE_INSTRUCTOR")) {
                    userRole = "INSTRUCTOR";
                    break;
                } else if (role.getName().equals("ROLE_ADMIN")) {
                    userRole = "ADMIN";
                    break;
                }
            }
        }

        return HomeworkChatMessageResponse.builder()
                .id(message.getId())
                .submissionId(message.getHomeworkSubmission().getId())
                .userId(message.getUser().getId())
                .userName(message.getUser().getFirstName() + " " + message.getUser().getLastName())
                .userRole(userRole)
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .build();
    }
}