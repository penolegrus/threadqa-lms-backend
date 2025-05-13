package com.threadqa.lms.repository.user;

import com.threadqa.lms.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryExtensions {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByTelegramConfirmationCode(String confirmationCode);

    Page<User> findByIsActiveFalse(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(String roleName, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(String roleName);
}
