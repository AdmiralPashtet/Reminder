package ru.admiralpashtet.reminder.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.service.UserService;
import ru.admiralpashtet.reminder.util.JwtUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Фильтр для обработки запросов, которые приходят уже после авторизации. С Github приходит не JWT, а opaque token.
 * Клеймы вытаскиваются запросом на отдельный энждпоинт Github API (в нашем случае - email).
 */
@Component
@RequiredArgsConstructor
public class GithubOauth2AuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final WebClient.Builder webClient;
    @Value("${urls.github-fetch-email}")
    private String githubFetchEmailUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
            if (!JwtUtil.isJwt(token)) {
                String email = fetchEmailFromGithub(token);
                User user = userService.createOrGetByEmail(email);
                CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                filterChain.doFilter(requestWrapper(request), response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * По дефолту в настройках профиля Github почта может быть приватной. Для получения адреса нужно дернуть отдельный
     * API, предварительно передав в запрос заголовок Authorization с полученным Access token.
     */
    private String fetchEmailFromGithub(String token) {
        return webClient.build()
                .get()
                .uri(githubFetchEmailUrl)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .flatMap(emails -> emails.stream()
                        .filter(entry -> Boolean.TRUE.equals(entry.get("primary")) &&
                                Boolean.TRUE.equals(entry.get("verified")))
                        .map(emailEntry -> emailEntry.get("email").toString())
                        .findFirst()
                        .map(Mono::just)
                        .orElseThrow(() -> new RuntimeException("Verified email was not found")))
                .block();
    }

    /**
     * Удаляем заголовок Authorization для предотвращения дальнейшей обработки токена как JWT.
     */
    private HttpServletRequest requestWrapper(HttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    return null;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    return Collections.emptyEnumeration();
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> headerNames = Collections.list(super.getHeaderNames());
                headerNames.removeIf(header -> header.equalsIgnoreCase(HttpHeaders.AUTHORIZATION));
                return Collections.enumeration(headerNames);
            }
        };
    }
}
