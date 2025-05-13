package com.threadqa.lms.service.stream;

import com.threadqa.lms.dto.stream.*;
import com.threadqa.lms.exception.AccessDeniedException;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.StreamMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.stream.Stream;
import com.threadqa.lms.model.stream.StreamParticipant;
import com.threadqa.lms.model.stream.StreamStatus;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseEnrollmentRepository;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.stream.StreamParticipantRepository;
import com.threadqa.lms.repository.stream.StreamRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления стримами (онлайн-трансляциями)
 */
@Service
public class StreamService {
    
    private final StreamRepository streamRepository;
    private final StreamParticipantRepository participantRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final StreamMapper streamMapper;
    private final NotificationService notificationService;
    
    @Autowired
    public StreamService(
            StreamRepository streamRepository,
            StreamParticipantRepository participantRepository,
            CourseRepository courseRepository,
            CourseEnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            StreamMapper streamMapper,
            NotificationService notificationService) {
        this.streamRepository = streamRepository;
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.streamMapper = streamMapper;
        this.notificationService = notificationService;
    }
    
    /**
     * Создает новый стрим
     * 
     * @param request данные для создания стрима
     * @param instructorId ID преподавателя, создающего стрим
     * @return информация о созданном стриме
     */
    @Transactional
    public StreamResponse createStream(StreamRequest request, Long instructorId) {
        // Находим преподавателя
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель не найден"));
        
        // Создаем объект стрима из запроса
        Stream stream = streamMapper.toEntity(request);
        stream.setInstructor(instructor);
        
        // Если указан курс, связываем стрим с ним
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Курс не найден"));
            stream.setCourse(course);
        }
        
        // Сохраняем стрим в базе данных
        Stream savedStream = streamRepository.save(stream);
        
        // Уведомляем участников курса о новом стриме
        if (request.getCourseId() != null) {
            notifyStreamCreated(savedStream);
        }
        
        return streamMapper.toResponse(savedStream);
    }
    
    /**
     * Получает информацию о стриме по ID
     * 
     * @param streamId ID стрима
     * @return информация о стриме
     */
    @Transactional(readOnly = true)
    public StreamResponse getStream(Long streamId) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Стрим не найден"));
        
        return streamMapper.toResponse(stream);
    }
    
    /**
     * Получает список всех стримов с пагинацией
     * 
     * @param pageable параметры пагинации
     * @return страница со списком стримов
     */
    @Transactional(readOnly = true)
    public Page<StreamResponse> getAllStreams(Pageable pageable) {
        Page<Stream> streams = streamRepository.findAll(pageable);
        return streams.map(streamMapper::toResponse);
    }
    
    /**
     * Получает список стримов для конкретного курса
     * 
     * @param courseId ID курса
     * @param pageable параметры пагинации
     * @return страница со списком стримов курса
     */
    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByCourse(Long courseId, Pageable pageable) {
        Page<Stream> streams = streamRepository.findByCourseId(courseId, pageable);
        return streams.map(streamMapper::toResponse);
    }
    
    /**
     * Получает список стримов конкретного преподавателя
     * 
     * @param instructorId ID преподавателя
     * @param pageable параметры пагинации
     * @return страница со списком стримов преподавателя
     */
    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByInstructor(Long instructorId, Pageable pageable) {
        Page<Stream> streams = streamRepository.findByInstructorId(instructorId, pageable);
        return streams.map(streamMapper::toResponse);
    }
    
    /**
     * Получает список стримов с определенным статусом
     * 
     * @param status статус стрима (SCHEDULED, LIVE, COMPLETED, CANCELLED)
     * @param pageable параметры пагинации
     * @return страница со списком стримов с указанным статусом
     */
    @Transactional(readOnly = true)
    public Page<StreamResponse> getStreamsByStatus(String status, Pageable pageable) {
        try {
            StreamStatus streamStatus = StreamStatus.valueOf(status.toUpperCase());
            Page<Stream> streams = streamRepository.findByStatus(streamStatus, pageable);
            return streams.map(streamMapper::toResponse);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректный статус стрима: " + status);
        }
    }
    
    /**
     * Получает список предстоящих стримов
     * 
     * @return список предстоящих стримов
     */
    @Transactional(readOnly = true)
    public List<StreamResponse> getUpcomingStreams() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(7); // Получаем стримы, запланированные на ближайшие 7 дней
        List<Stream> streams = streamRepository.findUpcomingStreams(threshold);
        return streamMapper.toResponseList(streams);
    }
    
    /**
     * Получает список активных (идущих в данный момент) стримов
     * 
     * @return список активных стримов
     */
    @Transactional(readOnly = true)
    public List<StreamResponse> getLiveStreams() {
        List<Stream> streams = streamRepository.findLiveStreams();
        return streamMapper.toResponseList(streams);
    }
    
    /**
     * Обновляет информацию о стриме
     * 
     * @param streamId ID стрима
     * @param request новые данные стрима
     * @param instructorId ID преподавателя, обновляющего стрим
     * @return обновленная информация о стриме
     */
    @Transactional
    public StreamResponse updateStream(Long streamId, StreamRequest request, Long instructorId) {
        // Проверяем, что стрим существует и принадлежит преподавателю
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("У вас нет прав на обновление этого стрима"));
        
        // Обновляем данные стрима
        stream.setTitle(request.getTitle());
        stream.setDescription(request.getDescription());
        stream.setStreamUrl(request.getStreamUrl());
        stream.setScheduledStartTime(request.getScheduledStartTime());
        stream.setIsRecorded(request.getIsRecorded());
        
        // Обновляем связь с курсом, если указан
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Курс не найден"));
            stream.setCourse(course);
        } else {
            stream.setCourse(null);
        }
        
        // Сохраняем обновленный стрим
        Stream updatedStream = streamRepository.save(stream);
        
        // Уведомляем участников об обновлении
        notifyStreamUpdated(updatedStream);
        
        return streamMapper.toResponse(updatedStream);
    }
    
    /**
     * Обновляет статус стрима
     * 
     * @param streamId ID стрима
     * @param request запрос на обновление статуса
     * @param instructorId ID преподавателя
     * @return обновленная информация о стриме
     */
    @Transactional
    public StreamResponse updateStreamStatus(Long streamId, StreamStatusUpdateRequest request, Long instructorId) {
        // Проверяем, что стрим существует и принадлежит преподавателю
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("У вас нет прав на обновление этого стрима"));
        
        try {
            // Преобразуем строковый статус в enum
            StreamStatus newStatus = StreamStatus.valueOf(request.getStatus().toUpperCase());
            stream.setStatus(newStatus);
            
            // Обновляем временные метки в зависимости от статуса
            if (newStatus == StreamStatus.LIVE && stream.getActualStartTime() == null) {
                stream.setActualStartTime(LocalDateTime.now());
            } else if (newStatus == StreamStatus.COMPLETED && stream.getEndTime() == null) {
                stream.setEndTime(LocalDateTime.now());
            }
            
            // Сохраняем обновленный стрим
            Stream updatedStream = streamRepository.save(stream);
            
            // Уведомляем участников об изменении статуса
            notifyStreamStatusChanged(updatedStream);
            
            return streamMapper.toResponse(updatedStream);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректный статус стрима: " + request.getStatus());
        }
    }
    
    /**
     * Добавляет ссылку на запись стрима
     * 
     * @param streamId ID стрима
     * @param request запрос с ссылкой на запись
     * @param instructorId ID преподавателя
     * @return обновленная информация о стриме
     */
    @Transactional
    public StreamResponse addStreamRecording(Long streamId, StreamRecordingRequest request, Long instructorId) {
        // Проверяем, что стрим существует и принадлежит преподавателю
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("У вас нет прав на обновление этого стрима"));
        
        // Проверяем, что стрим помечен как записываемый
        if (!stream.isRecorded()) {
            throw new BadRequestException("Этот стрим не помечен как записываемый");
        }
        
        // Добавляем ссылку на запись
        stream.setRecordingUrl(request.getRecordingUrl());
        Stream updatedStream = streamRepository.save(stream);
        
        // Уведомляем участников о добавлении записи
        notifyStreamRecordingAdded(updatedStream);
        
        return streamMapper.toResponse(updatedStream);
    }
    
    /**
     * Удаляет стрим
     * 
     * @param streamId ID стрима
     * @param instructorId ID преподавателя
     */
    @Transactional
    public void deleteStream(Long streamId, Long instructorId) {
        // Проверяем, что стрим существует и принадлежит преподавателю
        Stream stream = streamRepository.findByIdAndInstructorId(streamId, instructorId)
                .orElseThrow(() -> new AccessDeniedException("У вас нет прав на удаление этого стрима"));
        
        // Сначала удаляем всех участников
        List<StreamParticipant> participants = participantRepository.findByStreamId(streamId);
        participantRepository.deleteAll(participants);
        
        // Затем удаляем сам стрим
        streamRepository.delete(stream);
        
        // Уведомляем участников об отмене стрима
        notifyStreamCancelled(stream);
    }
    
    /**
     * Присоединяет пользователя к стриму
     * 
     * @param streamId ID стрима
     * @param userId ID пользователя
     * @return информация об участнике стрима
     */
    @Transactional
    public StreamParticipantResponse joinStream(Long streamId, Long userId) {
        // Находим стрим и пользователя
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Стрим не найден"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        
        // Проверяем, что стрим активен
        if (stream.getStatus() != StreamStatus.LIVE) {
            throw new BadRequestException("Стрим не активен");
        }
        
        // Проверяем, является ли пользователь уже участником
        Optional<StreamParticipant> existingParticipant = participantRepository.findByStreamIdAndUserId(streamId, userId);
        
        if (existingParticipant.isPresent()) {
            StreamParticipant participant = existingParticipant.get();
            
            // Если участник ранее покинул стрим, обновляем его статус
            if (!participant.isActive()) {
                participant.setActive(true);
                participant.setJoinedAt(LocalDateTime.now());
                participant.setLeftAt(null);
                participantRepository.save(participant);
            }
            
            return streamMapper.toParticipantResponse(participant);
        } else {
            // Создаем нового участника
            StreamParticipant participant = StreamParticipant.builder()
                    .stream(stream)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            
            StreamParticipant savedParticipant = participantRepository.save(participant);
            return streamMapper.toParticipantResponse(savedParticipant);
        }
    }
    
    /**
     * Отмечает, что пользователь покинул стрим
     * 
     * @param streamId ID стрима
     * @param userId ID пользователя
     * @return обновленная информация об участнике
     */
    @Transactional
    public StreamParticipantResponse leaveStream(Long streamId, Long userId) {
        // Находим участника
        StreamParticipant participant = participantRepository.findByStreamIdAndUserId(streamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Участник не найден"));
        
        // Отмечаем, что участник покинул стрим
        participant.setActive(false);
        participant.setLeftAt(LocalDateTime.now());
        
        StreamParticipant updatedParticipant = participantRepository.save(participant);
        return streamMapper.toParticipantResponse(updatedParticipant);
    }
    
    /**
     * Получает список всех участников стрима
     * 
     * @param streamId ID стрима
     * @return список участников
     */
    @Transactional(readOnly = true)
    public List<StreamParticipantResponse> getStreamParticipants(Long streamId) {
        List<StreamParticipant> participants = participantRepository.findByStreamId(streamId);
        return streamMapper.toParticipantResponseList(participants);
    }
    
    /**
     * Получает список активных участников стрима
     * 
     * @param streamId ID стрима
     * @return список активных участников
     */
    @Transactional(readOnly = true)
    public List<StreamParticipantResponse> getActiveStreamParticipants(Long streamId) {
        List<StreamParticipant> participants = participantRepository.findActiveParticipantsByStreamId(streamId);
        return streamMapper.toParticipantResponseList(participants);
    }
    
    // Вспомогательные методы для отправки уведомлений
    
    /**
     * Отправляет уведомления о создании нового стрима
     * 
     * @param stream созданный стрим
     */
    private void notifyStreamCreated(Stream stream) {
        if (stream.getCourse() != null) {
            // Получаем всех студентов, записанных на курс
            List<Long> enrolledUserIds = enrollmentRepository.findByCourseId(stream.getCourse().getId())
                    .stream()
                    .map(enrollment -> enrollment.getUser().getId())
                    .collect(Collectors.toList());
            
            // Формируем сообщение
            String title = "Новый стрим запланирован";
            String message = "Новый стрим \"" + stream.getTitle() + 
                    "\" запланирован на " + stream.getScheduledStartTime().toString();
            String type = "STREAM_SCHEDULED";
            String link = "/streams/" + stream.getId();
            
            // Отправляем уведомление каждому студенту
            for (Long userId : enrolledUserIds) {
                notificationService.createNotification(userId, title, message, type, link);
            }
        }
    }
    
    /**
     * Отправляет уведомления об обновлении стрима
     * 
     * @param stream обновленный стрим
     */
    private void notifyStreamUpdated(Stream stream) {
        if (stream.getCourse() != null) {
            // Получаем всех студентов, записанных на курс
            List<Long> enrolledUserIds = enrollmentRepository.findByCourseId(stream.getCourse().getId())
                    .stream()
                    .map(enrollment -> enrollment.getUser().getId())
                    .collect(Collectors.toList());
            
            // Формируем сообщение
            String title = "Стрим обновлен";
            String message = "Информация о стриме \"" + stream.getTitle() + 
                    "\" была обновлена. Новое время: " + stream.getScheduledStartTime().toString();
            String type = "STREAM_UPDATED";
            String link = "/streams/" + stream.getId();
            
            // Отправляем уведомление каждому студенту
            for (Long userId : enrolledUserIds) {
                notificationService.createNotification(userId, title, message, type, link);
            }
        }
    }
    
    /**
     * Отправляет уведомления об изменении статуса стрима
     * 
     * @param stream стрим с обновленным статусом
     */
    private void notifyStreamStatusChanged(Stream stream) {
        if (stream.getCourse() != null) {
            // Получаем всех студентов, записанных на курс
            List<Long> enrolledUserIds = enrollmentRepository.findByCourseId(stream.getCourse().getId())
                    .stream()
                    .map(enrollment -> enrollment.getUser().getId())
                    .collect(Collectors.toList());
            
            String title;
            String message;
            String type;
            
            // Формируем сообщение в зависимости от статуса
            if (stream.getStatus() == StreamStatus.LIVE) {
                title = "Стрим начался";
                message = "Стрим \"" + stream.getTitle() + "\" начался! Присоединяйтесь сейчас.";
                type = "STREAM_STARTED";
            } else if (stream.getStatus() == StreamStatus.COMPLETED) {
                title = "Стрим завершен";
                message = "Стрим \"" + stream.getTitle() + "\" завершен.";
                type = "STREAM_ENDED";
            } else {
                return; // Для других статусов не отправляем уведомления
            }
            
            String link = "/streams/" + stream.getId();
            
            // Отправляем уведомление каждому студенту
            for (Long userId : enrolledUserIds) {
                notificationService.createNotification(userId, title, message, type, link);
            }
        }
    }
    
    /**
     * Отправляет уведомления о добавлении записи стрима
     * 
     * @param stream стрим с добавленной записью
     */
    private void notifyStreamRecordingAdded(Stream stream) {
        if (stream.getCourse() != null) {
            // Получаем всех студентов, записанных на курс
            List<Long> enrolledUserIds = enrollmentRepository.findByCourseId(stream.getCourse().getId())
                    .stream()
                    .map(enrollment -> enrollment.getUser().getId())
                    .collect(Collectors.toList());
            
            // Формируем сообщение
            String title = "Доступна запись стрима";
            String message = "Запись стрима \"" + stream.getTitle() + "\" теперь доступна для просмотра.";
            String type = "STREAM_RECORDING";
            String link = "/streams/" + stream.getId() + "/recording";
            
            // Отправляем уведомление каждому студенту
            for (Long userId : enrolledUserIds) {
                notificationService.createNotification(userId, title, message, type, link);
            }
        }
    }
    
    /**
     * Отправляет уведомления об отмене стрима
     * 
     * @param stream отмененный стрим
     */
    private void notifyStreamCancelled(Stream stream) {
        if (stream.getCourse() != null) {
            // Получаем всех студентов, записанных на курс
            List<Long> enrolledUserIds = enrollmentRepository.findByCourseId(stream.getCourse().getId())
                    .stream()
                    .map(enrollment -> enrollment.getUser().getId())
                    .collect(Collectors.toList());
            
            // Формируем сообщение
            String title = "Стрим отменен";
            String message = "Стрим \"" + stream.getTitle() + "\", запланированный на " + 
                    stream.getScheduledStartTime().toString() + ", был отменен.";
            String type = "STREAM_CANCELLED";
            String link = "/courses/" + stream.getCourse().getId();
            
            // Отправляем уведомление каждому студенту
            for (Long userId : enrolledUserIds) {
                notificationService.createNotification(userId, title, message, type, link);
            }
        }
    }
}
