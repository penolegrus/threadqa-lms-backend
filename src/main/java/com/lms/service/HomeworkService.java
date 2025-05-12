package com.lms.service;

import com.lms.dto.homework.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.HomeworkMapper;
import com.lms.model.*;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public HomeworkResponse getHomework(Long homeworkId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        List<HomeworkRequirement> requirements = requirementRepository.findByHomework(homework);
        List<HomeworkRequirementResponse> requirementResponses = requirements.stream()
                .map(homeworkMapper::toHomeworkRequirementResponse)
                .collect(Collectors.toList());

        Integer submissionCount = submissionRepository.findByHomeworkId(homeworkId).size();

        return homeworkMapper.toHomeworkResponse(homework, requirementResponses, submissionCount);
    }

    @Transactional(readOnly = true)
    public List<HomeworkResponse> getHomeworksByTopic(Long topicId) {
        List<Homework> homeworks = homeworkRepository.findByTopicId(topicId);

        return homeworks.stream()
                .map(homework -> {
                    List<HomeworkRequirement> requirements = requirementRepository.findByHomework(homework);
                    List<HomeworkRequirementResponse> requirementResponses = requirements.stream()
                            .map(homeworkMapper::toHomeworkRequirementResponse)
                            .collect(Collectors.toList());

                    Integer submissionCount = submissionRepository.findByHomeworkId(homework.getId()).size();

                    return homeworkMapper.toHomeworkResponse(homework, requirementResponses, submissionCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public HomeworkResponse createHomework(HomeworkRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Homework homework = Homework.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .topic(topic)
                .dueDate(request.getDueDate())
                .maxPoints(request.getMaxPoints())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        Homework savedHomework = homeworkRepository.save(homework);

        List<HomeworkRequirementResponse> requirementResponses = new ArrayList<>();

        // Create requirements if provided
        if (request.getRequirements() != null && !request.getRequirements().isEmpty()) {
            for (HomeworkRequirementRequest reqRequest : request.getRequirements()) {
                HomeworkRequirement requirement = HomeworkRequirement.builder()
                        .homework(savedHomework)
                        .description(reqRequest.getDescription())
                        .points(reqRequest.getPoints())
                        .isRequired(reqRequest.getIsRequired() != null ? reqRequest.getIsRequired() : true)
                        .build();

                HomeworkRequirement savedRequirement = requirementRepository.save(requirement);
                requirementResponses.add(homeworkMapper.toHomeworkRequirementResponse(savedRequirement));
            }
        }

        return homeworkMapper.toHomeworkResponse(savedHomework, requirementResponses, 0);
    }

    @Transactional
    public HomeworkResponse updateHomework(Long homeworkId, HomeworkRequest request) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        // Check if topic is changing
        if (!homework.getTopic().getId().equals(request.getTopicId())) {
            Topic newTopic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            homework.setTopic(newTopic);
        }

        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setDueDate(request.getDueDate());
        homework.setMaxPoints(request.getMaxPoints());
        homework.setUpdatedAt(ZonedDateTime.now());

        Homework updatedHomework = homeworkRepository.save(homework);

        // Get existing requirements
        List<HomeworkRequirement> existingRequirements = requirementRepository.findByHomework(updatedHomework);
        List<HomeworkRequirementResponse> requirementResponses = existingRequirements.stream()
                .map(homeworkMapper::toHomeworkRequirementResponse)
                .collect(Collectors.toList());

        Integer submissionCount = submissionRepository.findByHomeworkId(homeworkId).size();

        return homeworkMapper.toHomeworkResponse(updatedHomework, requirementResponses, submissionCount);
    }

    @Transactional
    public void deleteHomework(Long homeworkId) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        // Check if there are submissions
        List<HomeworkSubmission> submissions = submissionRepository.findByHomeworkId(homeworkId);
        if (!submissions.isEmpty()) {
            throw new IllegalStateException("Cannot delete homework with submissions");
        }

        // Delete requirements first
        List<HomeworkRequirement> requirements = requirementRepository.findByHomework(homework);
        requirementRepository.deleteAll(requirements);

        // Delete homework
        homeworkRepository.delete(homework);
    }

    @Transactional
    public HomeworkRequirementResponse addHomeworkRequirement(HomeworkRequirementRequest request) {
        Homework homework = homeworkRepository.findById(request.getHomeworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        HomeworkRequirement requirement = HomeworkRequirement.builder()
                .homework(homework)
                .description(request.getDescription())
                .points(request.getPoints())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .build();

        HomeworkRequirement savedRequirement = requirementRepository.save(requirement);

        return homeworkMapper.toHomeworkRequirementResponse(savedRequirement);
    }

    @Transactional
    public void deleteHomeworkRequirement(Long requirementId) {
        HomeworkRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework requirement not found"));

        requirementRepository.delete(requirement);
    }

    @Transactional
    public HomeworkSubmissionResponse submitHomework(HomeworkSubmissionRequest request, Long userId) {
        Homework homework = homeworkRepository.findById(request.getHomeworkId())
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user has already submitted this homework
        HomeworkSubmission existingSubmission = submissionRepository.findByHomeworkAndUser(homework, user)
                .orElse(null);

        HomeworkSubmission submission;
        if (existingSubmission != null) {
            // Update existing submission
            existingSubmission.setSubmissionText(request.getSubmissionText());
            existingSubmission.setGithubUrl(request.getGithubUrl());
            existingSubmission.setAdditionalNotes(request.getAdditionalNotes());
            existingSubmission.setSubmittedAt(ZonedDateTime.now());
            submission = submissionRepository.save(existingSubmission);
        } else {
            // Create new submission
            submission = HomeworkSubmission.builder()
                    .homework(homework)
                    .user(user)
                    .submissionText(request.getSubmissionText())
                    .githubUrl(request.getGithubUrl())
                    .additionalNotes(request.getAdditionalNotes())
                    .isGraded(false)
                    .submittedAt(ZonedDateTime.now())
                    .build();
            submission = submissionRepository.save(submission);
        }

        List<String> fileUrls = new ArrayList<>(); // Will be populated when files are uploaded
        List<HomeworkChatMessageResponse> recentMessages = new ArrayList<>(); // No messages yet for new submission

        return homeworkMapper.toHomeworkSubmissionResponse(submission, fileUrls, recentMessages);
    }

    @Transactional
    public HomeworkSubmissionResponse uploadSubmissionFiles(Long submissionId, List<MultipartFile> files, Long userId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework submission not found"));

        // Check if user is authorized
        if (!submission.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to upload files to this submission");
        }

        List<String> fileUrls = new ArrayList<>();

        // Upload files
        for (MultipartFile file : files) {
            String fileUrl = fileStorageService.storeFile(file, "homework/" + submissionId);
            fileUrls.add(fileUrl);
        }

        // Get recent messages
        List<HomeworkChatMessage> chatMessages = chatMessageRepository.findByHomeworkSubmissionOrderBySentAtAsc(submission);
        List<HomeworkChatMessageResponse> recentMessages = chatMessages.stream()
                .map(homeworkMapper::toHomeworkChatMessageResponse)
                .collect(Collectors.toList());

        return homeworkMapper.toHomeworkSubmissionResponse(submission, fileUrls, recentMessages);
    }

    @Transactional(readOnly = true)
    public HomeworkSubmissionResponse getHomeworkSubmission(Long submissionId, Long userId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework submission not found"));

        // Check if user is authorized (either the submitter or an instructor/admin)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAuthorized = submission.getUser().getId().equals(userId) ||
                user.getRoles().stream().anyMatch(role ->
                        role.getName().equals("ROLE_INSTRUCTOR") || role.getName().equals("ROLE_ADMIN"));

        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to view this submission");
        }

        // Get file URLs (in a real implementation, you would retrieve these from storage)
        List<String> fileUrls = new ArrayList<>();

        // Get chat messages
        List<HomeworkChatMessage> chatMessages = chatMessageRepository.findByHomeworkSubmissionOrderBySentAtAsc(submission);
        List<HomeworkChatMessageResponse> recentMessages = chatMessages.stream()
                .map(homeworkMapper::toHomeworkChatMessageResponse)
                .collect(Collectors.toList());

        return homeworkMapper.toHomeworkSubmissionResponse(submission, fileUrls, recentMessages);
    }

    @Transactional(readOnly = true)
    public Page<HomeworkSubmissionResponse> getUserHomeworkSubmissions(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<HomeworkSubmission> submissions = submissionRepository.findByUser(user, pageable);

        List<HomeworkSubmissionResponse> submissionResponses = submissions.getContent().stream()
                .map(submission -> {
                    List<String> fileUrls = new ArrayList<>(); // In a real implementation, retrieve from storage

                    List<HomeworkChatMessage> chatMessages = chatMessageRepository.findByHomeworkSubmissionOrderBySentAtAsc(submission);
                    List<HomeworkChatMessageResponse> recentMessages = chatMessages.stream()
                            .map(homeworkMapper::toHomeworkChatMessageResponse)
                            .collect(Collectors.toList());

                    return homeworkMapper.toHomeworkSubmissionResponse(submission, fileUrls, recentMessages);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(submissionResponses, pageable, submissions.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<HomeworkSubmissionResponse> getHomeworkSubmissions(Long homeworkId, Pageable pageable) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        Page<HomeworkSubmission> submissions = submissionRepository.findByHomework(homework, pageable);

        List<HomeworkSubmissionResponse> submissionResponses = submissions.getContent().stream()
                .map(submission -> {
                    List<String> fileUrls = new ArrayList<>(); // In a real implementation, retrieve from storage

                    List<HomeworkChatMessage> chatMessages = chatMessageRepository.findByHomeworkSubmissionOrderBySentAtAsc(submission);
                    List<HomeworkChatMessageResponse> recentMessages = chatMessages.stream()
                            .map(homeworkMapper::toHomeworkChatMessageResponse)
                            .collect(Collectors.toList());

                    return homeworkMapper.toHomeworkSubmissionResponse(submission, fileUrls, recentMessages);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(submissionResponses, pageable, submissions.getTotalElements());
    }

    @Transactional
    public HomeworkSubmissionResponse reviewHomeworkSubmission(Long submissionId, HomeworkReviewRequest request) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework submission not found"));

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setIsGraded(true);
        submission.setGradedAt(ZonedDateTime.now());

        HomeworkSubmission gradedSubmission = submissionRepository.save(submission);

        // Get file URLs (in a real implementation, you would retrieve these from storage)
        List<String> fileUrls = new ArrayList<>();

        // Get chat messages
        List<HomeworkChatMessage> chatMessages = chatMessageRepository.findByHomeworkSubmissionOrderBySentAtAsc(gradedSubmission);
        List<HomeworkChatMessageResponse> recentMessages = chatMessages.stream()
                .map(homeworkMapper::toHomeworkChatMessageResponse)
                .collect(Collectors.toList());

        return homeworkMapper.toHomeworkSubmissionResponse(gradedSubmission, fileUrls, recentMessages);
    }

    @Transactional
    public HomeworkChatMessageResponse sendHomeworkChatMessage(Long submissionId, HomeworkChatMessageRequest request, Long userId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework submission not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is authorized (either the submitter or an instructor/admin)
        boolean isAuthorized = submission.getUser().getId().equals(userId) ||
                user.getRoles().stream().anyMatch(role ->
                        role.getName().equals("ROLE_INSTRUCTOR") || role.getName().equals("ROLE_ADMIN"));

        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to send messages for this submission");
        }

        HomeworkChatMessage chatMessage = HomeworkChatMessage.builder()
                .homeworkSubmission(submission)
                .user(user)
                .message(request.getMessage())
                .sentAt(ZonedDateTime.now())
                .build();

        HomeworkChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return homeworkMapper.toHomeworkChatMessageResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<HomeworkChatMessageResponse> getHomeworkChatMessages(Long submissionId, Long userId) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework submission not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is authorized (either the submitter or an instructor/admin)
        boolean isAuthorized = submission.getUser().getId().equals(userId) ||
                user.getRoles().stream().anyMatch(role ->
                        role.getName().equals("ROLE_INSTRUCTOR") || role.getName().equals("ROLE_ADMIN"));

        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to view messages for this submission");
        }

        List<HomeworkChatMessage> chatMessages = chatMessageRepository.findByHomeworkSubmissionOrderBySentAtAsc(submission);

        return chatMessages.stream()
                .map(homeworkMapper::toHomeworkChatMessageResponse)
                .collect(Collectors.toList());
    }
}