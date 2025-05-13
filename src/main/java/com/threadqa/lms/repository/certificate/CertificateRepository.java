package com.threadqa.lms.repository.certificate;

import com.threadqa.lms.model.certificate.Certificate;
import com.threadqa.lms.model.course.Course;
import com.threadqa.lms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    List<Certificate> findByUser(User user);
    
    List<Certificate> findByCourse(Course course);
    
    Optional<Certificate> findByUserAndCourse(User user, Course course);
    
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    @Query("SELECT c FROM Certificate c WHERE c.user.id = :userId")
    List<Certificate> findByUserId(Long userId);
    
    @Query("SELECT c FROM Certificate c WHERE c.course.id = :courseId")
    List<Certificate> findByCourseId(Long courseId);
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.course.id = :courseId")
    long countByCourseId(Long courseId);
    
    boolean existsByUserAndCourse(User user, Course course);
}
