package com.lms.repository;

import com.lms.model.Certificate;
import com.lms.model.Course;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Page<Certificate> findByUser(User user, Pageable pageable);

    Page<Certificate> findByCourse(Course course, Pageable pageable);

    Optional<Certificate> findByVerificationCode(String verificationCode);

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    Optional<Certificate> findByUserAndCourse(User user, Course course);
}