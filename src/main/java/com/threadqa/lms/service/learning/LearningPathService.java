package com.threadqa.lms.service.learning;

import com.threadqa.lms.dto.learningpath.*;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.LearningPathMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.course.CourseEnrollment;
import com.threadqa.lms.model.learning.LearningPath;
import com.threadqa.lms.model.learning.LearningPathItem;
import com.threadqa.lms.model.learning.UserLearningPathProgress;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.learning.LearningPathItemRepository;
import com.threadqa.lms.repository.learning.LearningPathRepository;
import com.threadqa.lms.repository.learning.UserLearningPathProgressRepository;
import com.threadqa.lms.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Сервис для управления образовательными путями
 * <p>
 * Предоставляет методы для создания, получения, обновления и удаления образовательных путей,
 * а также для управления элементами путей и прогрессом пользователей
 */
@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathItemRepository learningPathItemRepository;
    private final UserLearningPathProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserRepository userRepository;
    private final LearningPathMapper mapper;

    /**
     * Получает все опубликованные образовательные пути с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница с образовательными путями
     */
    public Page<LearningPathResponse> getAllPublishedLearningPaths(Pageable pageable) {
        return learningPathRepository.findByPublishedTrue(pageable)
                .map(mapper::toResponse);
    }

    /**
     * Получает все рекомендуемые образовательные пути с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница с рекомендуемыми образовательными путями
     */
    public Page<LearningPathResponse> getFeaturedLearningPaths(Pageable pageable) {
        return learningPathRepository.findByPublishedTrueAndFeaturedTrue(pageable)
                .map(mapper::toResponse);
    }

    /**
     * Поиск образовательных путей по ключевому слову
     *
     * @param keyword  Ключевое слово для поиска
     * @param pageable Параметры пагинации
     * @return Страница с найденными образовательными путями
     */
    public Page<LearningPathResponse> searchLearningPaths(String keyword, Pageable pageable) {
        return learningPathRepository.searchPublishedLearningPaths(keyword, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Получает образовательный путь по его URL-идентификатору (slug)
     *
     * @param slug URL-идентификатор образовательного пути
     * @return Данные образовательного пути с элементами
     * @throws ResourceNotFoundException если образовательный путь не найден
     */
    public LearningPathResponse getLearningPathBySlug(String slug) {
        LearningPath learningPath = learningPathRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с URL: " + slug));

        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);

        return mapper.toResponseWithItems(learningPath, items);
    }

    /**
     * Получает образовательный путь по его идентификатору
     *
     * @param id Идентификатор образовательного пути
     * @return Данные образовательного пути с элементами
     * @throws ResourceNotFoundException если образовательный путь не найден
     */
    public LearningPathResponse getLearningPathById(Long id) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + id));

        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);

        return mapper.toResponseWithItems(learningPath, items);
    }

    /**
     * Создает новый образовательный путь
     *
     * @param request Данные для создания образовательного пути
     * @param userId  Идентификатор пользователя-создателя
     * @return Данные созданного образовательного пути
     * @throws ResourceNotFoundException если пользователь не найден
     * @throws BadRequestException       если URL-идентификатор уже используется
     */
    @Transactional
    public LearningPathResponse createLearningPath(LearningPathRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с ID: " + userId));

        // Проверяем, не занят ли уже URL-идентификатор
        if (learningPathRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Образовательный путь с URL '" + request.getSlug() + "' уже существует");
        }

        LearningPath learningPath = mapper.toEntity(request, user);
        learningPath = learningPathRepository.save(learningPath);

        return mapper.toResponse(learningPath);
    }

    /**
     * Обновляет существующий образовательный путь
     *
     * @param id      Идентификатор образовательного пути
     * @param request Данные для обновления
     * @param userId  Идентификатор пользователя, выполняющего обновление
     * @return Обновленные данные образовательного пути
     * @throws ResourceNotFoundException если образовательный путь не найден
     * @throws BadRequestException       если пользователь не имеет прав на обновление или URL-идентификатор занят
     */
    @Transactional
    public LearningPathResponse updateLearningPath(Long id, LearningPathRequest request, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + id));

        // Проверяем, является ли пользователь создателем образовательного пути
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на редактирование этого образовательного пути");
        }

        // Проверяем, не занят ли новый URL-идентификатор другим образовательным путем
        if (!learningPath.getSlug().equals(request.getSlug()) &&
                learningPathRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Образовательный путь с URL '" + request.getSlug() + "' уже существует");
        }

        // Обновляем поля образовательного пути
        learningPath.setTitle(request.getTitle());
        learningPath.setDescription(request.getDescription());
        learningPath.setSlug(request.getSlug());
        learningPath.setDifficultyLevel(request.getDifficultyLevel());
        learningPath.setEstimatedHours(request.getEstimatedHours());
        learningPath.setPublished(request.isPublished());
        learningPath.setFeatured(request.isFeatured());
        learningPath.setThumbnailUrl(request.getThumbnailUrl());
        learningPath.setTags(request.getTags());

        learningPath = learningPathRepository.save(learningPath);

        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);

        return mapper.toResponseWithItems(learningPath, items);
    }

    /**
     * Удаляет образовательный путь
     *
     * @param id     Идентификатор образовательного пути
     * @param userId Идентификатор пользователя, выполняющего удаление
     * @throws ResourceNotFoundException если образовательный путь не найден
     * @throws BadRequestException       если пользователь не имеет прав на удаление
     */
    @Transactional
    public void deleteLearningPath(Long id, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + id));

        // Проверяем, является ли пользователь создателем образовательного пути
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление этого образовательного пути");
        }

        learningPathRepository.delete(learningPath);
    }

    /**
     * Добавляет курс в образовательный путь
     *
     * @param pathId  Идентификатор образовательного пути
     * @param request Данные для добавления курса
     * @param userId  Идентификатор пользователя, выполняющего добавление
     * @return Данные добавленного элемента
     * @throws ResourceNotFoundException если образовательный путь или курс не найден
     * @throws BadRequestException       если пользователь не имеет прав или курс уже добавлен
     */
    @Transactional
    public LearningPathItemResponse addItemToLearningPath(Long pathId, LearningPathItemRequest request, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + pathId));

        // Проверяем, является ли пользователь создателем образовательного пути
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на редактирование этого образовательного пути");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден с ID: " + request.getCourseId()));

        // Проверяем, есть ли уже курс в образовательном пути
        if (learningPathItemRepository.findByLearningPathOrderByPosition(learningPath).stream()
                .anyMatch(item -> item.getCourse().getId().equals(course.getId()))) {
            throw new BadRequestException("Курс уже добавлен в образовательный путь");
        }

        // Если позиция не указана, добавляем в конец
        if (request.getPosition() == null) {
            Integer maxPosition = learningPathItemRepository.findMaxPositionInPath(pathId);
            request.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        } else {
            // Сдвигаем существующие элементы, чтобы освободить место для нового
            learningPathItemRepository.incrementPositionsFromIndex(pathId, request.getPosition());
        }

        LearningPathItem item = mapper.toItemEntity(request, learningPath, course);
        item = learningPathItemRepository.save(item);

        return mapper.toItemResponse(item);
    }

    /**
     * Обновляет элемент образовательного пути
     *
     * @param pathId  Идентификатор образовательного пути
     * @param itemId  Идентификатор элемента
     * @param request Данные для обновления
     * @param userId  Идентификатор пользователя, выполняющего обновление
     * @return Обновленные данные элемента
     * @throws ResourceNotFoundException если образовательный путь, элемент или курс не найден
     * @throws BadRequestException       если пользователь не имеет прав или возникли другие ошибки
     */
    @Transactional
    public LearningPathItemResponse updateLearningPathItem(Long pathId, Long itemId, LearningPathItemRequest request, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + pathId));

        // Проверяем, является ли пользователь создателем образовательного пути
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на редактирование этого образовательного пути");
        }

        LearningPathItem item = learningPathItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Элемент образовательного пути не найден с ID: " + itemId));

        // Проверяем, принадлежит ли элемент указанному образовательному пути
        if (!item.getLearningPath().getId().equals(pathId)) {
            throw new BadRequestException("Элемент не принадлежит указанному образовательному пути");
        }

        // Если позиция меняется, обрабатываем изменение порядка
        if (request.getPosition() != null && !request.getPosition().equals(item.getPosition())) {
            // Удаляем со старой позиции
            learningPathItemRepository.decrementPositionsAfterIndex(pathId, item.getPosition());

            // Вставляем на новую позицию
            learningPathItemRepository.incrementPositionsFromIndex(pathId, request.getPosition());

            item.setPosition(request.getPosition());
        }

        // Обновляем другие поля
        item.setRequired(request.isRequired());
        item.setNotes(request.getNotes());

        // Если курс меняется, обновляем его
        if (!item.getCourse().getId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Курс не найден с ID: " + request.getCourseId()));

            // Проверяем, нет ли уже этого курса в образовательном пути
            if (learningPathItemRepository.findByLearningPathOrderByPosition(learningPath).stream()
                    .anyMatch(i -> i.getCourse().getId().equals(newCourse.getId()) && !i.getId().equals(itemId))) {
                throw new BadRequestException("Курс уже добавлен в образовательный путь");
            }

            item.setCourse(newCourse);
        }

        item = learningPathItemRepository.save(item);

        return mapper.toItemResponse(item);
    }

    /**
     * Удаляет элемент из образовательного пути
     *
     * @param pathId Идентификатор образовательного пути
     * @param itemId Идентификатор элемента
     * @param userId Идентификатор пользователя, выполняющего удаление
     * @throws ResourceNotFoundException если образовательный путь или элемент не найден
     * @throws BadRequestException       если пользователь не имеет прав или возникли другие ошибки
     */
    @Transactional
    public void removeLearningPathItem(Long pathId, Long itemId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + pathId));

        // Проверяем, является ли пользователь создателем образовательного пути
        if (!learningPath.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на редактирование этого образовательного пути");
        }

        LearningPathItem item = learningPathItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Элемент образовательного пути не найден с ID: " + itemId));

        // Проверяем, принадлежит ли элемент указанному образовательному пути
        if (!item.getLearningPath().getId().equals(pathId)) {
            throw new BadRequestException("Элемент не принадлежит указанному образовательному пути");
        }

        // Уменьшаем позиции элементов после удаляемого
        learningPathItemRepository.decrementPositionsAfterIndex(pathId, item.getPosition());

        // Удаляем элемент
        learningPathItemRepository.delete(item);
    }

    /**
     * Записывает пользователя на образовательный путь
     *
     * @param pathId Идентификатор образовательного пути
     * @param userId Идентификатор пользователя
     * @return Данные о прогрессе пользователя
     * @throws ResourceNotFoundException если образовательный путь или пользователь не найден
     * @throws BadRequestException       если пользователь уже записан на этот путь
     */
    @Transactional
    public UserLearningPathProgressResponse enrollUserInLearningPath(Long pathId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + pathId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с ID: " + userId));

        // Проверяем, не записан ли пользователь уже на этот путь
        if (progressRepository.findByUserAndLearningPath(user, learningPath).isPresent()) {
            throw new BadRequestException("Пользователь уже записан на этот образовательный путь");
        }

        // Создаем запись о прогрессе
        UserLearningPathProgress progress = UserLearningPathProgress.builder()
                .user(user)
                .learningPath(learningPath)
                .progressPercentage(0)
                .completed(false)
                .build();

        progress = progressRepository.save(progress);

        // Автоматически записываем на все обязательные курсы в образовательном пути
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);

        for (LearningPathItem item : items) {
            if (item.isRequired()) {
                Course course = item.getCourse();

                // Проверяем, не записан ли пользователь уже на курс
                if (courseEnrollmentRepository.findByUserAndCourse(user, course).isEmpty()) {
                    // Записываем пользователя на курс
                    CourseEnrollment enrollment = CourseEnrollment.builder()
                            .user(user)
                            .course(course)
                            .enrolledAt(ZonedDateTime.now())
                            .isActive(true)
                            .build();

                    courseEnrollmentRepository.save(enrollment);
                }
            }
        }

        return mapper.toProgressResponse(progress, items.size(), 0);
    }

    /**
     * Получает прогресс пользователя по образовательному пути
     *
     * @param pathId Идентификатор образовательного пути
     * @param userId Идентификатор пользователя
     * @return Данные о прогрессе пользователя
     * @throws ResourceNotFoundException если образовательный путь, пользователь или запись о прогрессе не найдены
     */
    public UserLearningPathProgressResponse getUserLearningPathProgress(Long pathId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + pathId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с ID: " + userId));

        UserLearningPathProgress progress = progressRepository.findByUserAndLearningPath(user, learningPath)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не записан на этот образовательный путь"));

        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);

        // Подсчитываем завершенные элементы
        AtomicInteger completedItems = new AtomicInteger();
        for (LearningPathItem item : items) {
            Course course = item.getCourse();

            // Проверяем, завершил ли пользователь курс
            courseEnrollmentRepository.findByUserAndCourse(user, course)
                    .ifPresent(enrollment -> {
                        if (enrollment.getCompletedAt() != null) {
                            completedItems.getAndIncrement();
                        }
                    });
        }

        return mapper.toDetailedProgressResponse(progress, items.size(), completedItems.get(), items);
    }

    /**
     * Получает все образовательные пути, на которые записан пользователь
     *
     * @param userId Идентификатор пользователя
     * @return Список данных о прогрессе пользователя по всем путям
     * @throws ResourceNotFoundException если пользователь не найден
     */
    public List<UserLearningPathProgressResponse> getUserEnrollments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с ID: " + userId));

        List<UserLearningPathProgress> progressList = progressRepository.findByUser(user);

        // Получаем все образовательные пути, на которые записан пользователь
        List<LearningPath> learningPaths = progressList.stream()
                .map(UserLearningPathProgress::getLearningPath)
                .collect(Collectors.toList());

        // Получаем все элементы для этих образовательных путей
        Map<Long, List<LearningPathItem>> itemsByPathId = learningPathItemRepository.findAll().stream()
                .filter(item -> learningPaths.contains(item.getLearningPath()))
                .collect(Collectors.groupingBy(item -> item.getLearningPath().getId()));

        // Получаем все записи пользователя на курсы
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByUser(user);
        Map<Long, CourseEnrollment> enrollmentsByCourseId = enrollments.stream()
                .collect(Collectors.toMap(e -> e.getCourse().getId(), e -> e, (e1, e2) -> e1));

        return progressList.stream()
                .map(progress -> {
                    Long pathId = progress.getLearningPath().getId();
                    List<LearningPathItem> items = itemsByPathId.getOrDefault(pathId, List.of());

                    // Подсчитываем завершенные курсы
                    int completedItems = (int) items.stream()
                            .filter(item -> {
                                CourseEnrollment enrollment = enrollmentsByCourseId.get(item.getCourse().getId());
                                return enrollment != null && enrollment.getCompletedAt() != null;
                            })
                            .count();

                    return mapper.toProgressResponse(progress, items.size(), completedItems);
                })
                .collect(Collectors.toList());
    }

    /**
     * Обновляет прогресс пользователя по образовательному пути
     *
     * @param pathId Идентификатор образовательного пути
     * @param userId Идентификатор пользователя
     * @return Обновленные данные о прогрессе
     * @throws ResourceNotFoundException если образовательный путь, пользователь или запись о прогрессе не найдены
     */
    @Transactional
    public UserLearningPathProgressResponse updateUserProgress(Long pathId, Long userId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Образовательный путь не найден с ID: " + pathId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с ID: " + userId));

        UserLearningPathProgress progress = progressRepository.findByUserAndLearningPath(user, learningPath)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не записан на этот образовательный путь"));

        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathOrderByPosition(learningPath);

        // Подсчитываем завершенные элементы и обновляем прогресс
        int totalItems = items.size();
        int completedItems = 0;

        for (LearningPathItem item : items) {
            Course course = item.getCourse();

            // Проверяем, завершил ли пользователь курс
            Optional<CourseEnrollment> enrollmentOpt = courseEnrollmentRepository.findByUserAndCourse(user, course);
            if (enrollmentOpt.isPresent() && enrollmentOpt.get().getCompletedAt() != null) {
                completedItems++;
            }
        }

        // Обновляем процент прогресса
        int progressPercentage = totalItems > 0 ? (completedItems * 100) / totalItems : 0;
        progress.setProgressPercentage(progressPercentage);

        // Проверяем, завершены ли все обязательные элементы
        boolean allRequiredCompleted = items.stream()
                .filter(LearningPathItem::isRequired)
                .allMatch(item -> {
                    Optional<CourseEnrollment> enrollment = courseEnrollmentRepository
                            .findByUserAndCourse(user, item.getCourse());
                    return enrollment.isPresent() && enrollment.get().getCompletedAt() != null;
                });

        // Отмечаем как завершенный, если все обязательные элементы выполнены
        if (allRequiredCompleted && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        progress = progressRepository.save(progress);

        return mapper.toProgressResponse(progress, totalItems, completedItems);
    }
}
