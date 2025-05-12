package com.lms.repository;

import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByEmailContainingOrFirstNameContainingOrLastNameContaining(
            String email, String firstName, String lastName, Pageable pageable);

    Page<User> findByRolesName(String roleName, Pageable pageable);

    Page<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRolesName(
            String email, String firstName, String lastName, String roleName, Pageable pageable);
}