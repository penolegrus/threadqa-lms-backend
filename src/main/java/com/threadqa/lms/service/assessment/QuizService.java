package com.threadqa.lms.service.assessment;

import com.threadqa.lms.dto.assessment.quiz.*;
import com.threadqa.lms.exception.AccessDeniedException;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.QuizMapper;
import com.threadqa.lms.model.assessment.*;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.assessment.*;
import com.threadqa.lms.repository.course.TopicRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import com.threadqa.lms.service.progress.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAnswerRepository answerRepository;
    private final QuizQuestionAnswerRepository questionAnswerRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final QuizMapper quizMapper;
    private final NotificationService notificationService;
    private final ProgressTrackingService progressTrackingService;

    @Transactional
    public QuizResponse createQuiz(QuizRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Quiz quiz = quizMapper.toEntity(request, topic, user);
        quiz = quizRepository.save(quiz);

        // Save questions and options
        saveQuestionsAndOptions(quiz, request.getQuestions());

        return quizMapper.toResponse(quiz);
    }

    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Check if user is authorized to update this quiz
        if (!quiz.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this quiz");
        }

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Update quiz properties
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setIsActive(request.getIsActive());
        quiz.setShuffleQuestions(request.getShuffleQuestions());
        quiz.setShowAnswers(request.getShowAnswers());
        quiz.setTopic(topic);

        quiz = quizRepository.save(quiz);

        // Delete existing questions and options
        List<QuizQuestion> existingQuestions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        for (QuizQuestion question : existingQuestions) {
            optionRepository.deleteByQuestionId(question.getId());
        }
        questionRepository.deleteByQuizId(quizId);

        // Save new questions and options
        saveQuestionsAndOptions(quiz, request.getQuestions());

        // Reload the quiz with all its questions and options
        quiz = quizRepository.findById(quizId).orElseThrow();

        return quizMapper.toResponse(quiz);
    }

    private void saveQuestionsAndOptions(Quiz quiz, List<QuizQuestionRequest> questionRequests) {
        for (QuizQuestionRequest questionRequest : questionRequests) {
            QuizQuestion question = quizMapper.toEntity(questionRequest, quiz);
            question = questionRepository.save(question);

            for (QuizOptionRequest optionRequest : questionRequest.getOptions()) {
                QuizOption option = quizMapper.toEntity(optionRequest, question);
                optionRepository.save(option);
            }
        }
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        QuizResponse response = quizMapper.toResponse(quiz);

        // Add statistics if the user is the creator
        if (quiz.getCreatedBy().getId().equals(userId)) {
            Long totalAttempts = answerRepository.count();
            Long passedAttempts = answerRepository.countPassedByQuizId(quizId);
            Double averageScore = answerRepository.getAverageScoreByQuizId(quizId);

            response.setTotalAttempts(totalAttempts.intValue());
            response.setPassedAttempts(passedAttempts.intValue());
            response.setAverageScore(averageScore);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizForStudent(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findActiveQuizById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found or not active"));

        QuizResponse response = quizMapper.toResponse(quiz);

        // Remove correct answers information for students
        response.getQuestions().forEach(question -> 
            question.getOptions().forEach(option -> option.setIsCorrect(null))
        );

        return response;
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByTopic(Long topicId, Pageable pageable) {
        Page<Quiz> quizzes = quizRepository.findAll(pageable);
        return quizzes.map(quizMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getActiveQuizzesByTopic(Long topicId) {
        List<Quiz> quizzes = quizRepository.findActiveQuizzesByTopicId(topicId);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getActiveQuizzesByCourse(Long courseId) {
        List<Quiz> quizzes = quizRepository.findActiveQuizzesByCourseId(courseId);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByCreator(Long userId, Pageable pageable) {
        Page<Quiz> quizzes = quizRepository.findByCreatedById(userId, pageable);
        return quizzes.map(quizMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> searchQuizzes(String searchTerm, Pageable pageable) {
        Page<Quiz> quizzes = quizRepository.searchQuizzes(searchTerm, pageable);
        return quizzes.map(quizMapper::toResponse);
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Check if user is authorized to delete this quiz
        if (!quiz.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to delete this quiz");
        }

        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizAnswerResponse startQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findActiveQuizById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found or not active"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already has an incomplete attempt
        List<QuizAnswer> existingAttempts = answerRepository.findByUserIdAndQuizId(userId, quizId);
        Optional<QuizAnswer> incompleteAttempt = existingAttempts.stream()
                .filter(attempt -> attempt.getCompletedAt() == null)
                .findFirst();

        if (incompleteAttempt.isPresent()) {
            QuizAnswer answer = incompleteAttempt.get();
            List<QuizQuestionAnswer> questionAnswers = questionAnswerRepository.findByQuizAnswerId(answer.getId());
            return quizMapper.toResponse(answer, questionAnswers);
        }

        // Create new quiz attempt
        QuizAnswerRequest request = new QuizAnswerRequest();
        request.setQuizId(quizId);
        
        QuizAnswer answer = quizMapper.toEntity(request, quiz, user);
        answer = answerRepository.save(answer);

        // Track activity
        progressTrackingService.trackActivity(userId, "QUIZ_START", "Started quiz: " + quiz.getTitle());

        return quizMapper.toResponse(answer, Collections.emptyList());
    }

    @Transactional
    public QuizAnswerResponse submitQuiz(Long quizId, QuizAnswerRequest request, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find the user's current attempt
        List<QuizAnswer> attempts = answerRepository.findByUserIdAndQuizId(userId, quizId);
        QuizAnswer answer = attempts.stream()
                .filter(attempt -> attempt.getCompletedAt() == null)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No active quiz attempt found"));

        // Process each question answer
        int totalPoints = 0;
        int earnedPoints = 0;
        List<QuizQuestionAnswer> questionAnswers = new ArrayList<>();

        for (QuizQuestionAnswerRequest qaRequest : request.getQuestionAnswers()) {
            QuizQuestion question = questionRepository.findById(qaRequest.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

            totalPoints += question.getPoints();

            // Get selected options
            List<QuizOption> selectedOptions = new ArrayList<>();
            if (qaRequest.getSelectedOptionIds() != null && !qaRequest.getSelectedOptionIds().isEmpty()) {
                selectedOptions = qaRequest.getSelectedOptionIds().stream()
                        .map(optionId -> optionRepository.findById(optionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Option not found")))
                        .collect(Collectors.toList());
            }

            // Calculate points earned for this question
            int pointsEarned = calculatePointsEarned(question, selectedOptions, qaRequest.getTextAnswer());
            earnedPoints += pointsEarned;

            // Save question answer
            QuizQuestionAnswer questionAnswer = quizMapper.toEntity(
                    qaRequest, answer, question, selectedOptions, pointsEarned);
            questionAnswer = questionAnswerRepository.save(questionAnswer);
            questionAnswers.add(questionAnswer);
        }

        // Calculate score as percentage
        int score = totalPoints > 0 ? (earnedPoints * 100) / totalPoints : 0;
        boolean isPassed = score >= quiz.getPassingScore();

        // Update quiz answer
        answer.setCompletedAt(LocalDateTime.now());
        answer.setScore(score);
        answer.setIsPassed(isPassed);
        answer = answerRepository.save(answer);

        // Track progress and send notification
        progressTrackingService.trackProgress(userId, "QUIZ_COMPLETE", quiz.getId(), score);
        progressTrackingService.trackActivity(userId, "QUIZ_SUBMIT", "Completed quiz: " + quiz.getTitle());

        if (isPassed) {
            notificationService.sendNotification(
                    userId, 
                    "Quiz Completed Successfully", 
                    "You passed the quiz '" + quiz.getTitle() + "' with a score of " + score + "%");
        } else {
            notificationService.sendNotification(
                    userId, 
                    "Quiz Completed", 
                    "You completed the quiz '" + quiz.getTitle() + "' with a score of " + score + "%");
        }

        return quizMapper.toResponse(answer, questionAnswers);
    }

    private int calculatePointsEarned(QuizQuestion question, List<QuizOption> selectedOptions, String textAnswer) {
        switch (question.getQuestionType()) {
            case SINGLE_CHOICE:
            case MULTIPLE_CHOICE:
                return calculateMultipleChoicePoints(question, selectedOptions);
            case TRUE_FALSE:
                return calculateTrueFalsePoints(question, selectedOptions);
            case SHORT_ANSWER:
                return calculateShortAnswerPoints(question, textAnswer);
            case MATCHING:
                return calculateMatchingPoints(question, selectedOptions);
            default:
                return 0;
        }
    }

    private int calculateMultipleChoicePoints(QuizQuestion question, List<QuizOption> selectedOptions) {
        List<QuizOption> correctOptions = question.getOptions().stream()
                .filter(QuizOption::getIsCorrect)
                .collect(Collectors.toList());

        // For single choice, there should be exactly one selected option that is correct
        if (question.getQuestionType() == QuizQuestion.QuestionType.SINGLE_CHOICE) {
            if (selectedOptions.size() != 1) {
                return 0;
            }
            return selectedOptions.get(0).getIsCorrect() ? question.getPoints() : 0;
        }

        // For multiple choice, all correct options must be selected and no incorrect ones
        if (selectedOptions.size() != correctOptions.size()) {
            return 0;
        }

        for (QuizOption option : selectedOptions) {
            if (!option.getIsCorrect()) {
                return 0;
            }
        }

        return question.getPoints();
    }

    private int calculateTrueFalsePoints(QuizQuestion question, List<QuizOption> selectedOptions) {
        if (selectedOptions.size() != 1) {
            return 0;
        }

        return selectedOptions.get(0).getIsCorrect() ? question.getPoints() : 0;
    }

    private int calculateShortAnswerPoints(QuizQuestion question, String textAnswer) {
        if (textAnswer == null || textAnswer.trim().isEmpty()) {
            return 0;
        }

        // Get the correct answer from the first option
        QuizOption correctOption = question.getOptions().stream()
                .filter(QuizOption::getIsCorrect)
                .findFirst()
                .orElse(null);

        if (correctOption == null) {
            return 0;
        }

        // Case-insensitive comparison
        return textAnswer.trim().equalsIgnoreCase(correctOption.getOptionText().trim()) 
                ? question.getPoints() : 0;
    }

    private int calculateMatchingPoints(QuizQuestion question, List<QuizOption> selectedOptions) {
        // For matching questions, we need to check if all pairs are correctly matched
        // This is a simplified implementation
        List<QuizOption> correctOptions = question.getOptions().stream()
                .filter(QuizOption::getIsCorrect)
                .collect(Collectors.toList());

        if (selectedOptions.size() != correctOptions.size()) {
            return 0;
        }

        for (QuizOption option : selectedOptions) {
            if (!option.getIsCorrect()) {
                return 0;
            }
        }

        return question.getPoints();
    }

    @Transactional(readOnly = true)
    public Page<QuizAnswerResponse> getUserQuizAttempts(Long userId, Pageable pageable) {
        Page<QuizAnswer> attempts = answerRepository.findByUserId(userId, pageable);
        return attempts.map(attempt -> {
            List<QuizQuestionAnswer> questionAnswers = questionAnswerRepository.findByQuizAnswerId(attempt.getId());
            return quizMapper.toResponse(attempt, questionAnswers);
        });
    }

    @Transactional(readOnly = true)
    public Page<QuizAnswerResponse> getQuizAttempts(Long quizId, Pageable pageable) {
        Page<QuizAnswer> attempts = answerRepository.findByQuizId(quizId, pageable);
        return attempts.map(attempt -> {
            List<QuizQuestionAnswer> questionAnswers = questionAnswerRepository.findByQuizAnswerId(attempt.getId());
            return quizMapper.toResponse(attempt, questionAnswers);
        });
    }

    @Transactional(readOnly = true)
    public QuizAnswerResponse getQuizAttempt(Long attemptId, Long userId) {
        QuizAnswer attempt = answerRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found"));

        // Check if user is authorized to view this attempt
        if (!attempt.getUser().getId().equals(userId) && !attempt.getQuiz().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to view this quiz attempt");
        }

        List<QuizQuestionAnswer> questionAnswers = questionAnswerRepository.findByQuizAnswerId(attemptId);
        return quizMapper.toResponse(attempt, questionAnswers);
    }

    @Transactional
    public QuizAnswerResponse provideFeedback(Long attemptId, String feedback, Long userId) {
        QuizAnswer attempt = answerRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found"));

        // Check if user is authorized to provide feedback
        if (!attempt.getQuiz().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to provide feedback for this quiz attempt");
        }

        attempt.setFeedback(feedback);
        attempt = answerRepository.save(attempt);

        // Send notification to the student
        notificationService.sendNotification(
                attempt.getUser().getId(),
                "Quiz Feedback Received",
                "You have received feedback for your quiz attempt on '" + attempt.getQuiz().getTitle() + "'");

        List<QuizQuestionAnswer> questionAnswers = questionAnswerRepository.findByQuizAnswerId(attemptId);
        return quizMapper.toResponse(attempt, questionAnswers);
    }

    @Transactional(readOnly = true)
    public QuizStatisticsResponse getQuizStatistics(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Check if user is authorized to view statistics
        if (!quiz.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to view statistics for this quiz");
        }

        List<QuizAnswer> allAttempts = answerRepository.findByQuizId(quizId, Pageable.unpaged()).getContent();
        
        int totalAttempts = allAttempts.size();
        int completedAttempts = (int) allAttempts.stream()
                .filter(a -> a.getCompletedAt() != null)
                .count();
        int passedAttempts = (int) allAttempts.stream()
                .filter(QuizAnswer::getIsPassed)
                .count();
        double passRate = completedAttempts > 0 ? (double) passedAttempts / completedAttempts * 100 : 0;
        double averageScore = allAttempts.stream()
                .filter(a -> a.getCompletedAt() != null)
                .mapToInt(QuizAnswer::getScore)
                .average()
                .orElse(0);
        
        // Calculate average time
        int averageTimeMinutes = (int) allAttempts.stream()
                .filter(a -> a.getCompletedAt() != null)
                .mapToLong(a -> java.time.Duration.between(a.getStartedAt(), a.getCompletedAt()).toMinutes())
                .average()
                .orElse(0);

        // Question statistics
        List<QuizStatisticsResponse.QuestionStatistics> questionStatsList = new ArrayList<>();
        for (QuizQuestion question : quiz.getQuestions()) {
            int questionAttempts = 0;
            int questionCorrect = 0;
            Map<String, Integer> optionDistribution = new HashMap<>();
            
            // Initialize option distribution
            for (QuizOption option : question.getOptions()) {
                optionDistribution.put(option.getOptionText(), 0);
            }
            
            // Analyze all answers for this question
            for (QuizAnswer attempt : allAttempts) {
                if (attempt.getCompletedAt() == null) continue;
                
                List<QuizQuestionAnswer> questionAnswers = questionAnswerRepository.findByQuizAnswerId(attempt.getId());
                Optional<QuizQuestionAnswer> questionAnswer = questionAnswers.stream()
                        .filter(qa -> qa.getQuestion().getId().equals(question.getId()))
                        .findFirst();
                
                if (questionAnswer.isPresent()) {
                    questionAttempts++;
                    if (questionAnswer.get().getPointsEarned().equals(question.getPoints())) {
                        questionCorrect++;
                    }
                    
                    // Count selected options
                    for (QuizOption selectedOption : questionAnswer.get().getSelectedOptions()) {
                        optionDistribution.put(
                                selectedOption.getOptionText(), 
                                optionDistribution.getOrDefault(selectedOption.getOptionText(), 0) + 1
                        );
                    }
                }
            }
            
            double correctRate = questionAttempts > 0 ? (double) questionCorrect / questionAttempts * 100 : 0;
            
            questionStatsList.add(QuizStatisticsResponse.QuestionStatistics.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .correctRate(correctRate)
                    .totalAttempts(questionAttempts)
                    .correctAttempts(questionCorrect)
                    .optionDistribution(optionDistribution)
                    .build());
        }

        return QuizStatisticsResponse.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalAttempts(totalAttempts)
                .completedAttempts(completedAttempts)
                .passedAttempts(passedAttempts)
                .passRate(passRate)
                .averageScore(averageScore)
                .averageTimeMinutes(averageTimeMinutes)
                .questionStatistics(questionStatsList)
                .build();
    }
}
