package com.threadqa.lms.service.payment;

import com.threadqa.lms.dto.payment.QrcIdDTO;
import com.threadqa.lms.dto.payment.QrcIdRequest;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.QrcIdMapper;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.payment.QrcId;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.course.CourseRepository;
import com.threadqa.lms.repository.payment.QrcIdRepository;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QrcIdService {

    private final QrcIdRepository qrcIdRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QrcIdMapper qrcIdMapper;

    @Transactional
    public QrcIdDTO generateQrcId(QrcIdRequest request, Long currentUserId) {
        User user = userRepository.findById(request.getUserId() != null ? request.getUserId() : currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        String qrcIdValue = generateUniqueQrcId();

        QrcId qrcId = new QrcId();
        qrcId.setQrcId(qrcIdValue);
        qrcId.setUser(user);
        qrcId.setCourse(course);
        qrcId.setCreatedAt(ZonedDateTime.now());
        qrcId.setExpiresAt(ZonedDateTime.now().plusHours(24)); // QR код действителен 24 часа
        qrcId.setIsUsed(false);

        QrcId savedQrcId = qrcIdRepository.save(qrcId);

        return qrcIdMapper.toQrcIdDTO(savedQrcId);
    }

    @Transactional(readOnly = true)
    public QrcIdDTO getQrcIdByCode(String qrcIdValue) {
        QrcId qrcId = qrcIdRepository.findByQrcId(qrcIdValue)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));

        return qrcIdMapper.toQrcIdDTO(qrcId);
    }

    @Transactional(readOnly = true)
    public List<QrcIdDTO> getQrcIdsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<QrcId> qrcIds = qrcIdRepository.findByUser(user);

        return qrcIds.stream()
                .map(qrcIdMapper::toQrcIdDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QrcIdDTO> getQrcIdsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        List<QrcId> qrcIds = qrcIdRepository.findByCourse(course);

        return qrcIds.stream()
                .map(qrcIdMapper::toQrcIdDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public QrcIdDTO markQrcIdAsUsed(String qrcIdValue) {
        QrcId qrcId = qrcIdRepository.findByQrcId(qrcIdValue)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));

        if (qrcId.getIsUsed()) {
            throw new IllegalStateException("QR code already used");
        }

        if (qrcId.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("QR code expired");
        }

        qrcId.setIsUsed(true);
        QrcId updatedQrcId = qrcIdRepository.save(qrcId);

        return qrcIdMapper.toQrcIdDTO(updatedQrcId);
    }

    private String generateUniqueQrcId() {
        String qrcIdValue;
        do {
            qrcIdValue = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        } while (qrcIdRepository.existsByQrcId(qrcIdValue));

        return qrcIdValue;
    }
}
