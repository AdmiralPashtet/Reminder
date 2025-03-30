package ru.admiralpashtet.reminder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.service.UserService;

import java.util.List;
import java.util.Map;


/**
 * Сервис обрабратывает данные, полученные от OAuth2-провайдера.
 * Извлекаем email, который используется в user-service для избежания дублирования сущностей.
 * Результат оборачивается в CustomUserPrincipal для передачи в атрибутах userId.
 */
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final WebClient.Builder webClient;
    @Value("${urls.github-fetch-email}")
    private String githubFetchEmailUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        if (email == null && userRequest.getClientRegistration().getRegistrationId().equals("github")) {
            email = fetchEmailFromGithub(userRequest);
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
        }

        User user = userService.createOrGetByEmail(email);
        return new CustomUserPrincipal(user, oAuth2User.getAttributes());
    }

    private String fetchEmailFromGithub(OAuth2UserRequest userRequest) {
        String token = userRequest.getAccessToken().getTokenValue();
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
}