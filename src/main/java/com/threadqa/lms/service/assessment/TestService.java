package com.threadqa.lms.service.assessment;

import com.threadqa.lms.dto.assessment.TestOptionRequest;
import com.threadqa.lms.dto.assessment.test.*;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.TestMapper;
import com.threadqa.lms.model.assessment.*;
import com.threadqa.lms.model.course.Topic;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.assessment.*;
import com.threadqa.lms.repository.course.TopicRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final TestQuestionRepository questionRepository;
    private final TestOptionRepository optionRepository;
    private final TestMatchingPairRepository matchingPairRepository;
    private final TestSubmissionRepository submissionRepository;
    private final TestAnswerRepository answerRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final TestMapper testMapper;

    @Transactional(readOnly = true)
    public TestResponse getTest(Long testId, boolean includeQuestions) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        return mapTestToResponse(test, includeQuestions);
    }

    @Transactional(readOnly = true)
    public Page<TestResponse> getTestsByTopic(Long topicId, Pageable pageable) {
        Page<Test> tests = testRepository.findByTopicId(topicId, pageable);

        return tests.map(test -> mapTestToResponse(test, false));
    }

    @Transactional(readOnly = true)
    public Page<TestResponse> getTestsByCourse(Long courseId, Pageable pageable) {
        Page<Test> tests = testRepository.findByCourseId(courseId, pageable);

        return tests.map(test -> mapTestToResponse(test, false));
    }

    @Transactional
    public TestResponse createTest(TestRequest request, Long currentUserId) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Проверка прав доступа - только инструктор курса или админ может создавать тесты
        if (!topic.getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to create tests for this topic");
        }

        Test test = Test.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .topic(topic)
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .passingScore(request.getPassingScore())
                .maxAttempts(request.getMaxAttempts())
                .isRandomized(request.getIsRandomized())
                .isPublished(request.getIsPublished())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        if (Boolean.TRUE.equals(request.getIsPublished())) {
            test.setPublishedAt(ZonedDateTime.now());
        }

        Test savedTest = testRepository.save(test);

        // Создание вопросов, если они предоставлены
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<TestQuestion> questions = new ArrayList<>();

            for (int i = 0; i < request.getQuestions().size(); i++) {
                TestQuestionRequest questionRequest = request.getQuestions().get(i);

                TestQuestion question = createTestQuestion(savedTest, questionRequest, i);
                questions.add(question);
            }

            savedTest.setQuestions(questions);
        }

        return mapTestToResponse(savedTest, true);
    }

    @Transactional
    public TestResponse updateTest(Long testId, TestRequest request, Long currentUserId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Проверка прав доступа - только инструктор курса или админ может обновлять тесты
        if (!test.getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this test");
        }

        // Проверка изменения темы
        if (!test.getTopic().getId().equals(request.getTopicId())) {
            Topic newTopic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

            // Проверка прав доступа для новой темы
            if (!newTopic.getCourse().getInstructor().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't have permission to move this test to the specified topic");
            }

            test.setTopic(newTopic);
        }

        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setTimeLimitMinutes(request.getTimeLimitMinutes());
        test.setPassingScore(request.getPassingScore());
        test.setMaxAttempts(request.getMaxAttempts());
        test.setIsRandomized(request.getIsRandomized());

        // Обновление статуса публикации
        if (Boolean.TRUE.equals(request.getIsPublished()) && !Boolean.TRUE.equals(test.getIsPublished())) {
            test.setIsPublished(true);
            test.setPublishedAt(ZonedDateTime.now());
        } else if (Boolean.FALSE.equals(request.getIsPublished())) {
            test.setIsPublished(false);
        }

        test.setUpdatedAt(ZonedDateTime.now());

        // Обновление вопросов, если они предоставлены
        if (request.getQuestions() != null) {
            // Удаление существующих вопросов
            List<TestQuestion> existingQuestions = questionRepository.findByTest(test);
            for (TestQuestion question : existingQuestions) {
                // Удаление опций
                optionRepository.deleteAll(question.getOptions());

                // Удаление пар для сопоставления
                matchingPairRepository.deleteAll(question.getMatchingPairs());
            }

            questionRepository.deleteAll(existingQuestions);

            // Создание новых вопросов
            List<TestQuestion> newQuestions = new ArrayList<>();

            for (int i = 0; i < request.getQuestions().size(); i++) {
                TestQuestionRequest questionRequest = request.getQuestions().get(i);

                TestQuestion question = createTestQuestion(test, questionRequest, i);
                newQuestions.add(question);
            }

            test.setQuestions(newQuestions);
        }

        Test updatedTest = testRepository.save(test);

        return mapTestToResponse(updatedTest, true);
    }

    @Transactional
    public void deleteTest(Long testId, Long currentUserId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Проверка прав доступа - только инструктор курса или админ может удалять тесты
        if (!test.getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this test");
        }

        // Проверка наличия отправленных решений
        Long submissionCount = submissionRepository.countByTestId(testId);
        if (submissionCount > 0) {
            throw new IllegalStateException("Cannot delete test with submissions");
        }

        // Удаление вопросов и связанных с ними данных
        List<TestQuestion> questions = questionRepository.findByTest(test);
        for (TestQuestion question : questions) {
            // Удаление опций
            optionRepository.deleteAll(question.getOptions());

            // Удаление пар для сопоставления
            matchingPairRepository.deleteAll(question.getMatchingPairs());
        }

        questionRepository.deleteAll(questions);

        // Удаление теста
        testRepository.delete(test);
    }

    @Transactional
    public TestSubmissionResponse submitTest(Long testId, TestSubmissionRequest request, Long currentUserId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверка максимального количества попыток
        if (test.getMaxAttempts() != null) {
            Long attemptCount = submissionRepository.findByUserAndTest(user, test).size();
            if (attemptCount >= test.getMaxAttempts()) {
                throw new IllegalStateException("Maximum number of attempts reached");
            }
        }

        // Определение номера попытки
        Integer attemptNumber = 1;
        var lastSubmission = submissionRepository.findTopByUserAndTestOrderByAttemptNumberDesc(user, test);
        if (lastSubmission.isPresent()) {
            attemptNumber = lastSubmission.get().getAttemptNumber() + 1;
        }

        // Создание отправки
        TestSubmission submission = TestSubmission.builder()
                .test(test)
                .user(user)
                .startedAt(request.getStartedAt())
                .submittedAt(ZonedDateTime.now())
                .attemptNumber(attemptNumber)
                .build();

        TestSubmission savedSubmission = submissionRepository.save(submission);

        // Обработка ответов
        List<TestAnswer> answers = new ArrayList<>();
        int totalScore = 0;

        for (TestAnswerRequest answerRequest : request.getAnswers()) {
            TestQuestion question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

            TestAnswer answer = TestAnswer.builder()
                    .submission(savedSubmission)
                    .question(question)
                    .textAnswer(answerRequest.getTextAnswer())
                    .codeAnswer(answerRequest.getCodeAnswer())
                    .build();

            // Обработка выбранных опций для вопросов с выбором
            if (answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty()) {
                List<TestOption> selectedOptions = answerRequest.getSelectedOptionIds().stream()
                        .map(optionId -> optionRepository.findById(optionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Option not found")))
                        .collect(Collectors.toList());

                answer.setSelectedOptions(selectedOptions);
            }

            // Автоматическая проверка для вопросов с выбором
            if (question.getQuestionType() == TestQuestion.QuestionType.SINGLE_CHOICE ||
                    question.getQuestionType() == TestQuestion.QuestionType.MULTIPLE_CHOICE ||
                    question.getQuestionType() == TestQuestion.QuestionType.TRUE_FALSE) {

                boolean isCorrect = checkMultipleChoiceAnswer(question, answer);
                answer.setIsCorrect(isCorrect);

                if (isCorrect) {
                    answer.setPointsEarned(question.getPoints());
                    totalScore += question.getPoints();
                } else {
                    answer.setPointsEarned(0);
                }
            } else {
                // Для других типов вопросов требуется ручная проверка
                answer.setIsCorrect(null);
                answer.setPointsEarned(null);
            }

            TestAnswer savedAnswer = answerRepository.save(answer);
            answers.add(savedAnswer);
        }

        // Обновление результата отправки
        boolean allAnswersGraded = answers.stream().allMatch(a -> a.getIsCorrect() != null);

        if (allAnswersGraded) {
            savedSubmission.setScore(totalScore);
            savedSubmission.setIsPassed(totalScore >= test.getPassingScore());
        }

        savedSubmission.setAnswers(answers);
        TestSubmission updatedSubmission = submissionRepository.save(savedSubmission);

        return testMapper.toTestSubmissionResponse(updatedSubmission);
    }

    @Transactional(readOnly = true)
    public TestSubmissionResponse getTestSubmission(Long submissionId, Long currentUserId) {
        TestSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Test submission not found"));

        // Проверка прав доступа - только владелец отправки или инструктор курса может просматривать отправку
        boolean isOwner = submission.getUser().getId().equals(currentUserId);
        boolean isInstructor = submission.getTest().getTopic().getCourse().getInstructor().getId().equals(currentUserId);

        if (!isOwner && !isInstructor) {
            throw new AccessDeniedException("You don't have permission to view this submission");
        }

        return testMapper.toTestSubmissionResponse(submission);
    }

    @Transactional(readOnly = true)
    public Page<TestSubmissionResponse> getTestSubmissionsByUser(Long userId, Pageable pageable, Long currentUserId) {
        // Проверка прав доступа - только владелец или инструктор/админ может просматривать отправки
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view these submissions");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<TestSubmission> submissions = submissionRepository.findByUser(user, pageable);

        return submissions.map(testMapper::toTestSubmissionResponse);
    }

    @Transactional(readOnly = true)
    public Page<TestSubmissionResponse> getTestSubmissionsByTest(Long testId, Pageable pageable, Long currentUserId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Проверка прав доступа - только инструктор курса или админ может просматривать все отправки теста
        if (!test.getTopic().getCourse().getInstructor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view these submissions");
        }

        Page<TestSubmission> submissions = submissionRepository.findByTest(test, pageable);

        return submissions.map(testMapper::toTestSubmissionResponse);
    }

    private TestQuestion createTestQuestion(Test test, TestQuestionRequest request, int orderIndex) {
        TestQuestion question = TestQuestion.builder()
                .test(test)
                .question(request.getQuestion())
                .questionType(request.getQuestionType())
                .points(request.getPoints())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : orderIndex)
                .codeSnippet(request.getCodeSnippet())
                .codeLanguage(request.getCodeLanguage())
                .build();

        TestQuestion savedQuestion = questionRepository.save(question);

        // Создание опций для вопросов с выбором
        if ((request.getQuestionType() == TestQuestion.QuestionType.SINGLE_CHOICE ||
                request.getQuestionType() == TestQuestion.QuestionType.MULTIPLE_CHOICE ||
                request.getQuestionType() == TestQuestion.QuestionType.TRUE_FALSE) &&
                request.getOptions() != null && !request.getOptions().isEmpty()) {

            List<TestOption> options = new ArrayList<>();

            for (int i = 0; i < request.getOptions().size(); i++) {
                TestOptionRequest optionRequest = request.getOptions().get(i);

                TestOption option = TestOption.builder()
                        .question(savedQuestion)
                        .content(optionRequest.getContent())
                        .isCorrect(optionRequest.getIsCorrect())
                        .orderIndex(optionRequest.getOrderIndex() != null ? optionRequest.getOrderIndex() : i)
                        .explanation(optionRequest.getExplanation())
                        .build();

                TestOption savedOption = optionRepository.save(option);
                options.add(savedOption);
            }

            savedQuestion.setOptions(options);
        }

        // Создание пар для вопросов на сопоставление
        if (request.getQuestionType() == TestQuestion.QuestionType.MATCHING &&
                request.getMatchingPairs() != null && !request.getMatchingPairs().isEmpty()) {

            List<TestMatchingPair> pairs = new ArrayList<>();

            for (int i = 0; i < request.getMatchingPairs().size(); i++) {
                TestMatchingPairRequest pairRequest = request.getMatchingPairs().get(i);

                TestMatchingPair pair = TestMatchingPair.builder()
                        .question(savedQuestion)
                        .leftItem(pairRequest.getLeftItem())
                        .rightItem(pairRequest.getRightItem())
                        .orderIndex(pairRequest.getOrderIndex() != null ? pairRequest.getOrderIndex() : i)
                        .build();

                TestMatchingPair savedPair = matchingPairRepository.save(pair);
                pairs.add(savedPair);
            }

            savedQuestion.setMatchingPairs(pairs);
        }

        return savedQuestion;
    }

    private boolean checkMultipleChoiceAnswer(TestQuestion question, TestAnswer answer) {
        List<TestOption> correctOptions = question.getOptions().stream()
                .filter(TestOption::getIsCorrect)
                .collect(Collectors.toList());

        List<TestOption> selectedOptions = answer.getSelectedOptions();

        if (question.getQuestionType() == TestQuestion.QuestionType.SINGLE_CHOICE ||
                question.getQuestionType() == TestQuestion.QuestionType.TRUE_FALSE) {
            // Для вопросов с одним правильным ответом
            if (selectedOptions.size() != 1) {
                return false;
            }

            return selectedOptions.get(0).getIsCorrect();
        } else if (question.getQuestionType() == TestQuestion.QuestionType.MULTIPLE_CHOICE) {
            // Для вопросов с множественным выбором
            if (selectedOptions.size() != correctOptions.size()) {
                return false;
            }

            return selectedOptions.containsAll(correctOptions);
        }

        return false;
    }

    private TestResponse mapTestToResponse(Test test, boolean includeQuestions) {
        Integer questionCount = (int) questionRepository.countByTestId(test.getId());
        Integer totalPoints = questionRepository.getTotalPointsByTestId(test.getId());
        Long submissionCount = submissionRepository.countByTestId(test.getId());

        List<TestQuestionResponse> questionResponses = null;
        if (includeQuestions) {
            List<TestQuestion> questions = questionRepository.findByTestOrderByOrderIndexAsc(test);
            questionResponses = questions.stream()
                    .map(testMapper::toTestQuestionResponse)
                    .collect(Collectors.toList());
        }

        return testMapper.toTestResponse(
                test,
                questionCount,
                totalPoints != null ? totalPoints : 0,
                submissionCount != null ? submissionCount.intValue() : 0,
                questionResponses
        );
    }
}