package com.threadqa.lms.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollmentRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private Long userId; // Может быть null, если берется из текущего аутентифицированного пользователя
}
