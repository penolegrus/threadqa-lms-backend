package com.threadqa.lms.mapper;

import com.threadqa.lms.dto.payment.QrcIdDTO;
import com.threadqa.lms.model.payment.QrcId;
import org.springframework.stereotype.Component;

@Component
public class QrcIdMapper {

    public QrcIdDTO toQrcIdDTO(QrcId qrcId) {
        if (qrcId == null) {
            return null;
        }

        return QrcIdDTO.builder()
                .id(qrcId.getId())
                .qrcId(qrcId.getQrcId())
                .userId(qrcId.getUser().getId())
                .userName(qrcId.getUser().getFirstName() + " " + qrcId.getUser().getLastName())
                .courseId(qrcId.getCourse().getId())
                .courseTitle(qrcId.getCourse().getTitle())
                .createdAt(qrcId.getCreatedAt())
                .expiresAt(qrcId.getExpiresAt())
                .isUsed(qrcId.getIsUsed())
                .build();
    }
}
