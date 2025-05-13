package com.threadqa.lms.service.user;

import com.threadqa.lms.dto.user.UserDTO;
import com.threadqa.lms.dto.user.UserProfileDTO;
import com.threadqa.lms.dto.user.UserUpdateRequest;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.UserMapper;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toUserDTO(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toUserProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        return users.map(userMapper::toUserDTO);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByRole(String roleName, Pageable pageable) {
        Page<User> users = userRepository.findByRoleName(roleName, pageable);

        return users.map(userMapper::toUserDTO);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UserUpdateRequest request, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверка прав доступа - только сам пользователь или админ может обновлять профиль
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this user");
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(ZonedDateTime.now());

        User updatedUser = userRepository.save(user);

        return userMapper.toUserDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверка прав доступа - только сам пользователь или админ может удалить профиль
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this user");
        }

        userRepository.delete(user);
    }
}