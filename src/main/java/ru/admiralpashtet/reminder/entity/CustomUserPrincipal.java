package ru.admiralpashtet.reminder.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Класс-обертка, объединяющая User и данные из OAuth2-провайдера.
 * Нужна для сохранения userId в сессию, чтобы в дальнейшем можно было использовать аннотацию @AuthenticationPrincipal
 * в сервисе.
 */

@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getId();
    }
}