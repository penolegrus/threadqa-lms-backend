package com.threadqa.lms.service.auth;

import com.threadqa.lms.dto.auth.AuthResponse;
import com.threadqa.lms.dto.auth.LoginRequest;
import com.threadqa.lms.dto.auth.RefreshTokenRequest;
import com.threadqa.lms.dto.auth.RegisterRequest;
import com.threadqa.lms.dto.user.UserDTO;
import com.threadqa.lms.exception.BadRequestException;
import com.threadqa.lms.exception.ResourceNotFoundException;
import com.threadqa.lms.mapper.UserMapper;
import com.threadqa.lms.model.user.Role;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.repository.user.RoleRepository;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }

        // Создание нового пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setCreatedAt(ZonedDateTime.now());
        user.setUpdatedAt(ZonedDateTime.now());

        // Установка ролей
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            });
        } else {
            // По умолчанию роль STUDENT
            Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
            roles.add(studentRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Отправка письма для подтверждения email
        emailService.sendVerificationEmail(savedUser);

        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерация токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserDTO userDTO = userMapper.toUserDTO(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 час в секундах
                .user(userDTO)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Получение пользователя и обновление времени последнего входа
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setLastLoginAt(ZonedDateTime.now());
        userRepository.save(user);

        // Генерация токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserDTO userDTO = userMapper.toUserDTO(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 час в секундах
                .user(userDTO)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // Проверка refresh токена
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadRequestException("Invalid refresh token");
        }

        // Получение ID пользователя из токена
        String userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Создание нового authentication объекта
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName()))
                        .toList()
        );

        // Генерация новых токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserDTO userDTO = userMapper.toUserDTO(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 час в секундах
                .user(userDTO)
                .build();
    }

    public void logout(String token) {
        // В JWT нет состояния, поэтому мы просто очищаем контекст безопасности
        SecurityContextHolder.clearContext();
        
        // Здесь можно добавить логику для blacklist токенов, если необходимо
    }

    @Transactional
    public void verifyEmail(String token) {
        // Проверка токена подтверждения email
        // Здесь должна быть логика проверки токена из базы данных или другого хранилища
        
        // Для примера, просто проверяем, что токен не пустой
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Invalid verification token");
        }
        
        // Получение пользователя по токену и подтверждение email
        // Здесь должна быть логика получения пользователя по токену
        
        // Для примера, просто обновляем статус email для пользователя с ID=1
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setIsEmailVerified(true);
        userRepository.save(user);
    }
}
