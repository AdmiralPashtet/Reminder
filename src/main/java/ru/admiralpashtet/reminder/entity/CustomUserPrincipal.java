package ru.admiralpashtet.reminder.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Класс-обертка, объединяющая User и данные из OAuth2-провайдера.
 * Нужна для сохранения userId в сессию, чтобы в дальнейшем можно было использовать аннотацию @AuthenticationPrincipal
 * в сервисе.
 */

@RequiredArgsConstructor
public class CustomUserPrincipal implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getId();
    }
}
