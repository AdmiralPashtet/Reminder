package ru.admiralpashtet.reminder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.service.UserService;

import java.io.IOException;
import java.util.Collections;

/**
 * Фильтр для обработки запросов, которые приходят уже после авторизации, когда у пользователя уже есть JWT токен,
 * который нужно валидировать и использовать для работы приложения.
 */
@Component
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token != null
                && jwtUtil.isValid(token)
        ) {
            String email = jwtUtil.getEmailFromToken(token);

            User user = userService.createOrGetByEmail(email);
            CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(user, Collections.emptyMap());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    customUserPrincipal, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
