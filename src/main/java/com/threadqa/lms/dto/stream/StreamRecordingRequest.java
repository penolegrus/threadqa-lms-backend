package com.threadqa.lms.dto.stream;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamRecordingRequest {
    
    @NotBlank(message = "Recording URL is required")
    private String recordingUrl;
}
