package com.threadqa.lms.controller.stream;

import com.threadqa.lms.dto.stream.*;
import com.threadqa.lms.security.CurrentUser;
import com.threadqa.lms.security.UserPrincipal;
import com.threadqa.lms.service.stream.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления стримами (онлайн-трансляциями)
 * Предоставляет API для создания, получения, обновления и удаления стримов,
 * а также для управления участниками стримов
 */
@RestController
@RequestMapping("/api/streams")
@Tag(name = "Управление стримами", description = "API для управления онлайн-трансляциями")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    /**
     * Создание нового стрима
     *
     * @param request     Данные для создания стрима
     * @param currentUser Текущий пользователь (преподаватель)
     * @return Информация о созданном стриме
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Создание нового стрима", description = "Создание новой онлайн-трансляции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Стрим успешно создан",
                    content = @Content(schema = @Schema(implementation = StreamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<StreamResponse> createStream(
            @Valid @RequestBody StreamRequest request,
            @CurrentUser UserPrincipal currentUser) {

        Long instructorId = currentUser.getId();
        StreamResponse response = streamService.createStream(request, instructorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Получение информации о стриме по ID
     *
     * @param id ID стрима
     * @return Информация о стриме
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получение стрима по ID", description = "Получение информации о стриме по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Стрим найден",
                    content = @Content(schema = @Schema(implementation = StreamResponse.class))),
            @ApiResponse(responseCode = "404", description = "Стрим не найден")
    })
    public ResponseEntity<StreamResponse> getStream(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id) {

        StreamResponse response = streamService.getStream(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение списка всех стримов с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница со списком стримов
     */
    @GetMapping
    @Operation(summary = "Получение всех стримов", description = "Получение списка всех стримов с пагинацией")
    public ResponseEntity<Page<StreamResponse>> getAllStreams(Pageable pageable) {
        Page<StreamResponse> streams = streamService.getAllStreams(pageable);
        return ResponseEntity.ok(streams);
    }

    /**
     * Получение списка стримов для конкретного курса
     *
     * @param courseId ID курса
     * @param pageable Параметры пагинации
     * @return Страница со списком стримов курса
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Получение стримов по курсу", description = "Получение всех стримов для конкретного курса")
    public ResponseEntity<Page<StreamResponse>> getStreamsByCourse(
            @Parameter(description = "ID курса") @PathVariable Long courseId,
            Pageable pageable) {

        Page<StreamResponse> streams = streamService.getStreamsByCourse(courseId, pageable);
        return ResponseEntity.ok(streams);
    }

    /**
     * Получение списка стримов конкретного преподавателя
     *
     * @param instructorId ID преподавателя
     * @param pageable     Параметры пагинации
     * @return Страница со списком стримов преподавателя
     */
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Получение стримов по преподавателю", description = "Получение всех стримов конкретного преподавателя")
    public ResponseEntity<Page<StreamResponse>> getStreamsByInstructor(
            @Parameter(description = "ID преподавателя") @PathVariable Long instructorId,
            Pageable pageable) {

        Page<StreamResponse> streams = streamService.getStreamsByInstructor(instructorId, pageable);
        return ResponseEntity.ok(streams);
    }

    /**
     * Получение списка стримов с определенным статусом
     *
     * @param status   Статус стрима (SCHEDULED, LIVE, COMPLETED, CANCELLED)
     * @param pageable Параметры пагинации
     * @return Страница со списком стримов с указанным статусом
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Получение стримов по статусу", description = "Получение всех стримов с определенным статусом")
    public ResponseEntity<Page<StreamResponse>> getStreamsByStatus(
            @Parameter(description = "Статус стрима (SCHEDULED, LIVE, COMPLETED, CANCELLED)")
            @PathVariable String status,
            Pageable pageable) {

        Page<StreamResponse> streams = streamService.getStreamsByStatus(status, pageable);
        return ResponseEntity.ok(streams);
    }

    /**
     * Получение списка предстоящих стримов
     *
     * @return Список предстоящих стримов
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Получение предстоящих стримов", description = "Получение всех запланированных стримов")
    public ResponseEntity<List<StreamResponse>> getUpcomingStreams() {
        List<StreamResponse> streams = streamService.getUpcomingStreams();
        return ResponseEntity.ok(streams);
    }

    /**
     * Получение списка активных (идущих в данный момент) стримов
     *
     * @return Список активных стримов
     */
    @GetMapping("/live")
    @Operation(summary = "Получение активных стримов", description = "Получение всех стримов, идущих в данный момент")
    public ResponseEntity<List<StreamResponse>> getLiveStreams() {
        List<StreamResponse> streams = streamService.getLiveStreams();
        return ResponseEntity.ok(streams);
    }

    /**
     * Обновление информации о стриме
     *
     * @param id          ID стрима
     * @param request     Новые данные стрима
     * @param currentUser Текущий пользователь (преподаватель)
     * @return Обновленная информация о стриме
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Обновление стрима", description = "Обновление информации о существующем стриме")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Стрим успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Стрим не найден")
    })
    public ResponseEntity<StreamResponse> updateStream(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id,
            @Valid @RequestBody StreamRequest request,
            @CurrentUser UserPrincipal currentUser) {

        Long instructorId = currentUser.getId();
        StreamResponse response = streamService.updateStream(id, request, instructorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление статуса стрима (запуск, завершение, отмена)
     *
     * @param id          ID стрима
     * @param request     Новый статус стрима
     * @param currentUser Текущий пользователь (преподаватель)
     * @return Обновленная информация о стриме
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Обновление статуса стрима", description = "Изменение статуса существующего стрима")
    public ResponseEntity<StreamResponse> updateStreamStatus(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id,
            @Valid @RequestBody StreamStatusUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        Long instructorId = currentUser.getId();
        StreamResponse response = streamService.updateStreamStatus(id, request, instructorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Добавление ссылки на запись стрима
     *
     * @param id          ID стрима
     * @param request     Данные о записи стрима
     * @param currentUser Текущий пользователь (преподаватель)
     * @return Обновленная информация о стриме
     */
    @PatchMapping("/{id}/recording")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Добавление записи стрима", description = "Добавление ссылки на запись завершенного стрима")
    public ResponseEntity<StreamResponse> addStreamRecording(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id,
            @Valid @RequestBody StreamRecordingRequest request,
            @CurrentUser UserPrincipal currentUser) {

        Long instructorId = currentUser.getId();
        StreamResponse response = streamService.addStreamRecording(id, request, instructorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление стрима
     *
     * @param id          ID стрима
     * @param currentUser Текущий пользователь (преподаватель)
     * @return Пустой ответ с кодом 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Удаление стрима", description = "Удаление существующего стрима")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Стрим успешно удален"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Стрим не найден")
    })
    public ResponseEntity<Void> deleteStream(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id,
            @CurrentUser UserPrincipal currentUser) {

        Long instructorId = currentUser.getId();
        streamService.deleteStream(id, instructorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Присоединение к стриму в качестве участника
     *
     * @param id          ID стрима
     * @param currentUser Текущий пользователь
     * @return Информация об участии в стриме
     */
    @PostMapping("/{id}/join")
    @Operation(summary = "Присоединение к стриму", description = "Присоединение к стриму в качестве участника")
    public ResponseEntity<StreamParticipantResponse> joinStream(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id,
            @CurrentUser UserPrincipal currentUser) {

        Long userId = currentUser.getId();
        StreamParticipantResponse response = streamService.joinStream(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Выход из стрима (для участника)
     *
     * @param id          ID стрима
     * @param currentUser Текущий пользователь
     * @return Обновленная информация об участии в стриме
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "Выход из стрима", description = "Выход из стрима для участника")
    public ResponseEntity<StreamParticipantResponse> leaveStream(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id,
            @CurrentUser UserPrincipal currentUser) {

        Long userId = currentUser.getId();
        StreamParticipantResponse response = streamService.leaveStream(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение списка всех участников стрима
     *
     * @param id ID стрима
     * @return Список участников стрима
     */
    @GetMapping("/{id}/participants")
    @Operation(summary = "Получение участников стрима", description = "Получение списка всех участников стрима")
    public ResponseEntity<List<StreamParticipantResponse>> getStreamParticipants(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id) {

        List<StreamParticipantResponse> participants = streamService.getStreamParticipants(id);
        return ResponseEntity.ok(participants);
    }

    /**
     * Получение списка активных участников стрима (тех, кто сейчас присутствует)
     *
     * @param id ID стрима
     * @return Список активных участников стрима
     */
    @GetMapping("/{id}/participants/active")
    @Operation(summary = "Получение активных участников стрима", description = "Получение списка всех активных участников стрима")
    public ResponseEntity<List<StreamParticipantResponse>> getActiveStreamParticipants(
            @Parameter(description = "ID стрима") @PathVariable("id") Long id) {

        List<StreamParticipantResponse> participants = streamService.getActiveStreamParticipants(id);
        return ResponseEntity.ok(participants);
    }
}
