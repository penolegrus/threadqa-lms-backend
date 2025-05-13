package com.threadqa.lms.service.homework;

import com.threadqa.lms.dto.homework.*;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.HomeworkMapper;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.homework.Homework;
import com.threadqa.lms.model.homework.HomeworkChatMessage;
import com.threadqa.lms.model.homework.HomeworkRequirement;
import com.threadqa.lms.model.homework.HomeworkSubmission;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.TopicRepository;
import com.threadqa.lms.repository.homework.HomeworkChatMessageRepository;
import com.threadqa.lms.repository.homework.HomeworkRepository;
import com.threadqa.lms.repository.homework.HomeworkRequirementRepository;
import com.threadqa.lms.repository.homework.HomeworkSubmissionRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final HomeworkRequirementRepository requirementRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final HomeworkChatMessageRepository chatMessageRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final HomeworkMapper homeworkMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<HomeworkResponse> getHomeworksByTopic(Long topicId, Pageable pageable, Long currentUserId) {
        Page<Homework> homeworks = homeworkRepository.findByTopicId(topicId, pageable);

        return homeworks.map(homework -> mapHomeworkToResponse(homework, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<HomeworkResponse> getHomeworksByCourse(Long courseId, Pageable pageable, Long currentUserId) {
        Page<Homework> homeworks = homeworkRepository.findByCourseId(courseId, pageable);

        return homeworks.map(homework -> mapHomeworkToResponse(homework, currentUserId));
    }

    @Transactional(readOnly = true)
    public HomeworkResponse getHomework(Long homeworkId, Long currentUserId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        return mapHomeworkToResponse(homework, currentUserId);
    }

    @Transactional
    public HomeworkResponse createHomework(HomeworkRequest request, Long currentUserId) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Проверка прав доступа - только инструктор курса или админ может создавать домашние задания
        if (!topic.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to create homework for this topic");
        }

        Homework homework = Homework.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .topic(topic)
                .maxScore(request.getMaxScore())
                .dueDate(request.getDueDate())
                .isPublished(request.getIsPublished())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        if (Boolean.TRUE.equals(request.getIsPublished())) {
            homework.setPublishedAt(ZonedDateTime.now());
        }

        Homework savedHomework = homeworkRepository.save(homework);

        // Создание требований, если они предоставлены
        if (request.getRequirements() != null && !request.getRequirements().isEmpty()) {
            List<HomeworkRequirement> requirements = new ArrayList<>();

            for (int i = 0; i < request.getRequirements().size(); i++) {
                HomeworkRequirementRequest requirementRequest = request.getRequirements().get(i);

                HomeworkRequirement requirement = HomeworkRequirement.builder()
                        .homework(savedHomework)
                        .description(requirementRequest.getDescription())
                        .orderIndex(requirementRequest.getOrderIndex() != null ? requirementRequest.getOrderIndex() : i)
                        .points(requirementRequest.getPoints())
                        .build();

                requirements.add(requirementRepository.save(requirement));
            }

            savedHomework.setRequirements(requirements);
        }

        return mapHomeworkToResponse(savedHomework, currentUserId);
    }

    @Transactional
    public HomeworkResponse updateHomework(Long homeworkId, HomeworkRequest request, Long currentUserId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        // Проверка прав доступа - только инструктор курса или админ может обновлять домашние задания
        if (!homework.getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this homework");
        }

        // Проверка изменения темы
        if (!homework.getTopic().getId().equals(request.getTopicId())) {
            Topic newTopic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

            // Проверка прав доступа для новой темы
            if (!newTopic.getCourse().getInstructor().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't have permission to move this homework to the specified topic");
            }

            homework.setTopic(newTopic);
        }

        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setMaxScore(request.getMaxScore());
        homework.setDueDate(request.getDueDate());

        // Обновление статуса публикации
        if (Boolean.TRUE.equals(request.getIsPublished()) && !Boolean.TRUE.equals(homework.getIsPublished())) {
            homework.setIsPublished(true);
            homework.setPublishedAt(ZonedDateTime.now());
        } else if (Boolean.FALSE.equals(request.getIsPublished())) {
            homework.setIsPublished(false);
        }

        homework.setUpdatedAt(ZonedDateTime.now());

        // Обновление требований, если они предоставлены
        if (request.getRequirements() != null) {
            // Удаление существующих требований
            requirementRepository.deleteByHomeworkId(homeworkId);

            // Создание новых требований
            List<HomeworkRequirement> newRequirements = new ArrayList<>();

            for (int i = 0; i < request.getRequirements().size(); i++) {
                HomeworkRequirementRequest requirementRequest = request.getRequirements().get(i);

                HomeworkRequirement requirement = HomeworkRequirement.builder()
                        .homework(homework)
                        .description(requirementRequest.getDescription())
                        .orderIndex(requirementRequest.getOrderIndex() != null ? requirementRequest.getOrderIndex() : i)
                        .points(requirementRequest.getPoints())
                        .build();

                newRequirements.add(requirementRepository.save(requirement));
            }

            homework.setRequirements(newRequirements);
        }

        Homework updatedHomework = homeworkRepository.save(homework);

        return mapHomeworkToResponse(updatedHomework, currentUserId);
    }

    @Transactional
    public void deleteHomework(Long homeworkId, Long currentUserId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        // Проверка прав доступа - только инструктор курса или админ может удалять домашние задания
        if (!homework.getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this homework");
        }

        // Проверка наличия отправленных решений
        Long submissionCount = submissionRepository.countByHomeworkId(homeworkId);
        if (submissionCount > 0) {
            throw new IllegalStateException("Cannot delete homework with submissions");
        }

        // Удаление требований
        requirementRepository.deleteByHomeworkId(homeworkId);

        // Удаление домашнего задания
        homeworkRepository.delete(homework);
    }

    @Transactional
    public HomeworkSubmissionResponse submitHomework(HomeworkSubmissionRequest request, Long currentUserId) {
        Homework homework = homeworkRepository.findById(request.getHomeworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверка, опубликовано ли домашнее задание
        if (!homework.getIsPublished()) {
            throw new IllegalStateException("Cannot submit to unpublished homework");
        }

        // Проверка, не истек ли срок сдачи
        if (homework.getDueDate() != null && homework.getDueDate().isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("Submission deadline has passed");
        }

        // Проверка, не отправлял ли пользователь уже решение
        Optional<HomeworkSubmission> existingSubmission = submissionRepository.findByHomeworkAndUser(homework, user);
        
        HomeworkSubmission submission;
        
        if (existingSubmission.isPresent()) {
            // Обновление существующего решения
            submission = existingSubmission.get();
            submission.setContent(request.getContent());
            
            // Обновление URL файлов
            if (request.getFileUrls() != null && !request.getFileUrls().isEmpty()) {
                submission.setFileUrls(String.join(",", request.getFileUrls()));
            } else {
                submission.setFileUrls(null);
            }
            
            submission.setSubmittedAt(ZonedDateTime.now());
            submission.setStatus(HomeworkSubmission.SubmissionStatus.SUBMITTED);
        } else {
            // Создание нового решения
            submission = HomeworkSubmission.builder()
                    .homework(homework)
                    .user(user)
                    .content(request.getContent())
                    .fileUrls(request.getFileUrls() != null && !request.getFileUrls().isEmpty() ? 
                            String.join(",", request.getFileUrls()) : null)
                    .submittedAt(ZonedDateTime.now())
                    .status(HomeworkSubmission.SubmissionStatus.SUBMITTED)
                    .build();
        }

        HomeworkSubmission savedSubmission = submissionRepository.save(submission);

        // Отправка уведомления инструктору курса
        notificationService.createNotification(
                homework.getTopic().getCourse().getInstructor().getId(),
                "Новое решение домашнего задания",
                user.getFirstName() + " " + user.getLastName() + " отправил решение для задания \"" + homework.getTitle() + "\"",
                "HOMEWORK_SUBMISSION",
                "/homeworks/" + homework.getId() + "/submissions/" + savedSubmission.getId()
        );

        return homeworkMapper.toHomeworkSubmissionResponse(savedSubmission, currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<HomeworkSubmissionResponse> getSubmissionsByHomework(Long homeworkId, Pageable pageable, Long currentUserId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        // Проверка прав доступа - только инструктор курса или админ может просматривать все решения
        if (!homework.getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view these submissions");
        }

        Page<HomeworkSubmission> submissions = submissionRepository.findByHomework(homework, pageable);

        return submissions.map(submission -> homeworkMapper.toHomeworkSubmissionResponse(submission, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<HomeworkSubmissionResponse> getSubmissionsByUser(Long userId, Pageable pageable, Long currentUserId) {
        // Проверка прав доступа - только сам пользователь или инструктор/админ может просматривать решения
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view these submissions");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<HomeworkSubmission> submissions = submissionRepository.findByUser(user, pageable);

        return submissions.map(submission -> homeworkMapper.toHomeworkSubmissionResponse(submission, currentUserId));
    }

    @Transactional(readOnly = true)
    public HomeworkSubmissionResponse getSubmission(Long submissionId, Long currentUserId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Проверка прав доступа - только автор решения или инструктор курса может просматривать решение
        boolean isAuthor = submission.getUser().getId().equals(currentUserId);
        boolean isInstructor = submission.getHomework().getTopic().getCourse().getInstructor().getId().equals(currentUserId);

        if (!isAuthor && !isInstructor) {
            throw new AccessDeniedException("You don't have permission to view this submission");
        }

        return homeworkMapper.toHomeworkSubmissionResponse(submission, currentUserId);
    }

    @Transactional
    public HomeworkSubmissionResponse reviewSubmission(Long submissionId, HomeworkReviewRequest request, Long currentUserId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Проверка прав доступа - только инструктор курса может проверять решения
        if (!submission.getHomework().getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to review this submission");
        }

        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        submission.setReviewer(reviewer);
        submission.setReviewedAt(ZonedDateTime.now());
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(request.getStatus());

        HomeworkSubmission updatedSubmission = submissionRepository.save(submission);

        // Отправка уведомления студенту
        notificationService.createNotification(
                submission.getUser().getId(),
                "Ваше решение проверено",
                "Ваше решение для задания \"" + submission.getHomework().getTitle() + "\" было проверено",
                "HOMEWORK_REVIEW",
                "/homeworks/" + submission.getHomework().getId() + "/submissions/" + submission.getId()
        );

        return homeworkMapper.toHomeworkSubmissionResponse(updatedSubmission, currentUserId);
    }

    @Transactional
    public HomeworkChatMessageResponse sendChatMessage(HomeworkChatMessageRequest request, Long currentUserId) {
        HomeworkSubmission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Проверка прав доступа - только автор решения или инструктор курса может отправлять сообщения
        boolean isAuthor = submission.getUser().getId().equals(currentUserId);
        boolean isInstructor = submission.getHomework().getTopic().getCourse().getInstructor().getId().equals(currentUserId);

        if (!isAuthor && !isInstructor) {
            throw new AccessDeniedException("You don't have permission to send messages in this chat");
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        HomeworkChatMessage message = HomeworkChatMessage.builder()
                .submission(submission)
                .sender(sender)
                .content(request.getContent())
                .fileUrls(request.getFileUrls() != null && !request.getFileUrls().isEmpty() ? 
                        String.join(",", request.getFileUrls()) : null)
                .sentAt(ZonedDateTime.now())
                .isRead(false)
                .build();

        HomeworkChatMessage savedMessage = chatMessageRepository.save(message);

        // Отправка уведомления получателю (инструктору или студенту)
        Long recipientId = isAuthor ? 
                submission.getHomework().getTopic().getCourse().getInstructor().getId() : 
                submission.getUser().getId();

        notificationService.createNotification(
                recipientId,
                "Новое сообщение по домашнему заданию",
                "У вас новое сообщение по заданию \"" + submission.getHomework().getTitle() + "\"",
                "HOMEWORK_MESSAGE",
                "/homeworks/" + submission.getHomework().getId() + "/submissions/" + submission.getId()
        );

        return homeworkMapper.toHomeworkChatMessageResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<HomeworkChatMessageResponse> getChatMessages(Long submissionId, Pageable pageable, Long currentUserId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Проверка прав доступа - только автор решения или инструктор курса может просматривать сообщения
        boolean isAuthor = submission.getUser().getId().equals(currentUserId);
        boolean isInstructor = submission.getHomework().getTopic().getCourse().getInstructor().getId().equals(currentUserId);

        if (!isAuthor && !isInstructor) {
            throw new AccessDeniedException("You don't have permission to view messages in this chat");
        }

        Page<HomeworkChatMessage> messages = chatMessageRepository.findBySubmissionOrderBySentAtDesc(submission, pageable);

        return messages.map(homeworkMapper::toHomeworkChatMessageResponse);
    }

    @Transactional
    public void markChatMessagesAsRead(Long submissionId, Long currentUserId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Проверка прав доступа - только автор решения или инструктор курса может отмечать сообщения как прочитанные
        boolean isAuthor = submission.getUser().getId().equals(currentUserId);
        boolean isInstructor = submission.getHomework().getTopic().getCourse().getInstructor().getId().equals(currentUserId);

        if (!isAuthor && !isInstructor) {
            throw new AccessDeniedException("You don't have permission to mark messages as read in this chat");
        }

        chatMessageRepository.markAllAsReadInSubmission(submissionId, currentUserId);
    }

    private HomeworkResponse mapHomeworkToResponse(Homework homework, Long currentUserId) {
        // Подсчет статистики по решениям
        Integer submissionCount = submissionRepository.countByHomeworkId(homework.getId()).intValue();
        Integer completedCount = submissionRepository.countCompletedByHomeworkId(homework.getId()).intValue();
        Double averageScore = submissionRepository.getAverageScoreByHomeworkId(homework.getId());
        if (averageScore == null) {
            averageScore = 0.0;
        }

        // Проверка, отправил ли текущий пользователь решение
        Boolean isSubmitted = false;
        HomeworkSubmissionResponse userSubmission = null;

        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                Optional<HomeworkSubmission> submission = submissionRepository.findByHomeworkAndUser(homework, currentUser);
                if (submission.isPresent()) {
                    isSubmitted = true;
                    userSubmission = homeworkMapper.toHomeworkSubmissionResponse(submission.get(), currentUserId);
                }
            }
        }

        // Получение требований
        List<HomeworkRequirementResponse> requirements = homework.getRequirements().stream()
                .map(homeworkMapper::toHomeworkRequirementResponse)
                .collect(Collectors.toList());

        return homeworkMapper.toHomeworkResponse(
                homework,
                submissionCount,
                completedCount,
                averageScore,
                isSubmitted,
                userSubmission,
                requirements
        );
    }
}
