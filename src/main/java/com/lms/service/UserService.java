package com.lms.service;

import com.lms.dto.user.UserDTO;
import com.lms.dto.user.UserProfileDTO;
import com.lms.dto.user.UserUpdateRequest;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.UserMapper;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserProfileDTO getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toUserProfileDTO(user);
    }

    @Transactional
    public UserProfileDTO updateUserProfile(Long userId, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());

        if (updateRequest.getTelegram() != null) {
            user.setTelegram(updateRequest.getTelegram());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserProfileDTO(updatedUser);
    }

    public Page<UserDTO> getAllUsers(String search, String role, Pageable pageable) {
        Page<User> users;

        if (search != null && !search.isEmpty() && role != null && !role.isEmpty()) {
            users = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRolesName(
                    search, search, search, role, pageable);
        } else if (search != null && !search.isEmpty()) {
            users = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContaining(
                    search, search, search, pageable);
        } else if (role != null && !role.isEmpty()) {
            users = userRepository.findByRolesName(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(userMapper::toUserDTO);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return userMapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO createUser(String email, String password, String firstName, String lastName,
                              String telegram, Set<String> roleNames) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTelegram(telegram);
        user.setEmailVerified(false);
        user.setTelegramConnected(false);
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            roles = roleNames.stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
        } else {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_USER"));
            roles.add(userRole);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        return userMapper.toUserDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());

        if (updateRequest.getTelegram() != null) {
            user.setTelegram(updateRequest.getTelegram());
        }

        user.setEnabled(updateRequest.isEnabled());

        if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {
            Set<Role> roles = updateRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Transactional
    public void resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void grantCourseAccess(Long userId, Long courseId) {
        // Implementation will be added when we create the CourseEnrollment entity
    }
}