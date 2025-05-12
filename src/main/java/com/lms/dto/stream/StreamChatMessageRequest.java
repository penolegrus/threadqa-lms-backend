package com.lms.dto.stream;

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
public class StreamChatMessageRequest {

    @NotNull(message = "Stream ID is required")
    private Long streamId;

    @NotBlank(message = "Message is required")
    private String message;
}