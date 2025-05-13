package com.threadqa.lms.dto.stream;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamParticipantRequest {
    
    @NotNull(message = "Stream ID is required")
    private Long streamId;
}
