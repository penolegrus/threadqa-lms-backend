package com.threadqa.lms.repository.payment;

import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.payment.QrcId;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrcIdRepository extends JpaRepository<QrcId, Long> {

    Optional<QrcId> findByQrcId(String qrcId);

    List<QrcId> findByUser(User user);

    List<QrcId> findByCourse(Course course);

    List<QrcId> findByUserAndCourse(User user, Course course);

    boolean existsByQrcId(String qrcId);
}
