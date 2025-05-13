package com.threadqa.lms.security;

import com.threadqa.lms.exception.AccountBlockedException;
import com.threadqa.lms.model.user.User;
import com.threadqa.lms.model.user.UserSession;
import com.threadqa.lms.repository.user.UserRepository;
import com.threadqa.lms.service.auth.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Проверяем, существует ли активная сессия с этим токеном
                Optional<UserSession> sessionOpt = sessionService.getSessionByToken(jwt);
                
                if (sessionOpt.isPresent()) {
                    UserSession session = sessionOpt.get();
                    
                    // Проверяем, не заблокирован ли пользователь
                    Long userId = session.getUserId();
                    User user = userRepository.findById(userId).orElse(null);
                    
                    if (user != null && !user.getIsActive()) {
                        throw new AccountBlockedException("Аккаунт заблокирован. Причина: " + 
                                (user.getBlockReason() != null ? user.getBlockReason() : "Неизвестно"));
                    }
                    
                    // Обновляем время последней активности сессии
                    sessionService.updateSessionActivity(jwt);
                    
                    // Устанавливаем аутентификацию
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("Активная сессия не найдена для токена");
                }
            }
        } catch (AccountBlockedException e) {
            log.warn("Попытка доступа к заблокированному аккаунту: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        } catch (Exception e) {
            log.error("Не удалось установить аутентификацию пользователя в контексте безопасности", e);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
