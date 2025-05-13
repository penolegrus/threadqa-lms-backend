package com.threadqa.lms.dto.assessment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestOptionRequest {

    private Long id; // Может быть null для новых опций

    @NotBlank(message = "Option content is required")
    private String content;

    @NotNull(message = "Correct flag is required")
    private Boolean isCorrect;

    private Integer orderIndex;

    private String explanation;
}