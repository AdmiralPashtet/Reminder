package ru.admiralpashtet.reminder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.service.UserService;

import java.io.IOException;
import java.util.Collections;

/**
 * Фильтр для обработки запросов, которые приходят уже после авторизации когда у пользователя уже есть JWT токен.
 * От Google получаем JWT токен, который под капотом парсим в JwtAuthenticationToken и сохраняем в Security Context.
 * <p/>
 * Прежде проверяем, что в заголовке лежит JWT токен, а не opaque токен от GitHub.
 */
@Component
@AllArgsConstructor
public class GoogleOauth2AuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Проверяем сохранен ли токен в Security Context в предшествующей цепочке фильтров
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                filterChain.doFilter(request, response);
                return;
            }
            // BearerTokenAuthenticationFilter.class парсит JWT токен из заголовка и кладет объект в Security Context
            if (authentication instanceof JwtAuthenticationToken token) {
                String email = token.getToken().getClaim("email");
                User user = userService.createOrGetByEmail(email);
                CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}