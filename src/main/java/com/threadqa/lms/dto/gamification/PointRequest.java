package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Point;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRequest {

    @NotNull
    @Positive
    private Integer amount;

    @NotNull
    private Point.PointType pointType;

    private String description;

    private String entityType;

    private Long entityId;
}
