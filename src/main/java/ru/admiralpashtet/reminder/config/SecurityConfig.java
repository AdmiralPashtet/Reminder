package ru.admiralpashtet.reminder.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import ru.admiralpashtet.reminder.filter.GithubOauth2AuthenticationFilter;
import ru.admiralpashtet.reminder.filter.GoogleOauth2AuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final GoogleOauth2AuthenticationFilter googleOauth2AuthenticationFilter;
    private final GithubOauth2AuthenticationFilter githubOauth2AuthenticationFilter;

    public SecurityConfig(GoogleOauth2AuthenticationFilter googleOauth2AuthenticationFilter,
                          GithubOauth2AuthenticationFilter githubOauth2AuthenticationFilter
    ) {
        this.googleOauth2AuthenticationFilter = googleOauth2AuthenticationFilter;
        this.githubOauth2AuthenticationFilter = githubOauth2AuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .anonymous(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/oauth2/callback/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2ResourceServer(token -> token.jwt(Customizer.withDefaults()))
                .addFilterBefore(githubOauth2AuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(googleOauth2AuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }
}