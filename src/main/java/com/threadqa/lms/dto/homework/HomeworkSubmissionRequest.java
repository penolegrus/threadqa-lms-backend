package com.threadqa.lms.dto.homework;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkSubmissionRequest {

    @NotNull(message = "Homework ID is required")
    private Long homeworkId;

    @NotBlank(message = "Content is required")
    private String content;

    private List<String> fileUrls;
}
