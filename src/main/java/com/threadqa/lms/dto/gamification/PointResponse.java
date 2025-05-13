package com.threadqa.lms.dto.gamification;

import com.threadqa.lms.model.gamification.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Integer amount;
    private Point.PointType pointType;
    private String description;
    private String entityType;
    private Long entityId;
    private ZonedDateTime createdAt;
}
