package com.lms.service;

import com.lms.dto.auth.*;
import com.lms.dto.user.UserDTO;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.TokenRefreshException;
import com.lms.exception.UserAlreadyExistsException;
import com.lms.mapper.UserMapper;
import com.lms.model.RefreshToken;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.model.VerificationToken;
import com.lms.repository.RefreshTokenRepository;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import com.lms.repository.VerificationTokenRepository;
import com.lms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Value("${app.email.verification-expiration-minutes}")
    private int verificationExpirationMinutes;

    @Value("${app.email.password-reset-expiration-minutes}")
    private int passwordResetExpirationMinutes;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toUserDTO(user))
                .build();
    }

    @Transactional
    public UserDTO register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + registerRequest.getEmail());
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setTelegram(registerRequest.getTelegram());
        user.setEmailVerified(false);
        user.setTelegramConnected(false);
        user.setEnabled(true);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_USER"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Handle referral code if provided
        if (registerRequest.getReferralCode() != null && !registerRequest.getReferralCode().isEmpty()) {
            // Process referral code
        }

        // Send verification email
        sendVerificationEmail(savedUser);

        return userMapper.toUserDTO(savedUser);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = tokenProvider.generateAccessToken(user);
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(requestRefreshToken)
                            .user(userMapper.toUserDTO(user))
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
    }

    @Transactional
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setType("EMAIL_VERIFICATION");
        verificationToken.setExpiresAt(ZonedDateTime.now().plusMinutes(verificationExpirationMinutes));

        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public void confirmEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndType(token, "EMAIL_VERIFICATION")
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (verificationToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new TokenRefreshException("Verification token expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();

        VerificationToken passwordResetToken = new VerificationToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setType("PASSWORD_RESET");
        passwordResetToken.setExpiresAt(ZonedDateTime.now().plusMinutes(passwordResetExpirationMinutes));

        verificationTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        VerificationToken passwordResetToken = verificationTokenRepository.findByTokenAndType(request.getToken(), "PASSWORD_RESET")
                .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token"));

        if (passwordResetToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            verificationTokenRepository.delete(passwordResetToken);
            throw new TokenRefreshException("Password reset token expired");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        verificationTokenRepository.delete(passwordResetToken);
    }

    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(ZonedDateTime.now().plusDays(30));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(ZonedDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expired. Please login again");
        }
        return token;
    }
}