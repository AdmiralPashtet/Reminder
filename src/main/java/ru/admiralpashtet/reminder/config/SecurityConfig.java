package ru.admiralpashtet.reminder.config;


import org.springframework.beans.factory.annotation.Value;
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
import ru.admiralpashtet.reminder.security.AuthenticationFilter;
import ru.admiralpashtet.reminder.service.impl.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthenticationFilter authenticationFilter;
    @Value("${jwt.github.secret}")
    private String githubSecret;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          AuthenticationFilter authenticationFilter
    ) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.authenticationFilter = authenticationFilter;
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
                .addFilterAfter(authenticationFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

//    @Bean
//    public JwtDecoder jwtDecoder() {
//        // Декодер для Google (асимметричная подпись, RS256)
//        NimbusJwtDecoder googleDecoder = NimbusJwtDecoder
//                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
//                .jwsAlgorithm(SignatureAlgorithm.RS256)
//                .build();
//
//        // Декодер для GitHub (с симметричной подписью, HS256)
//        NimbusJwtDecoder githubDecoder = NimbusJwtDecoder
//                .withSecretKey(new SecretKeySpec(githubSecret.getBytes(StandardCharsets.UTF_8), "HMACSHA256"))
//                .build();
//
//        // Делегирующий декодер: сперва пробуем Google, затем GitHub.
//        return token -> {
//            System.err.println(token);
//            try {
//                Jwt jwt = googleDecoder.decode(token);
//                if ("https://accounts.google.com".equals(jwt.getIssuer().toString())) {
//                    return jwt;
//                }
//            } catch (Exception ignored) {
//                // Если не получилось декодировать как Google, пробуем GitHub.
//            }
//            try {
//                return githubDecoder.decode(token);
//            } catch (Exception ex) {
//                throw new JwtException("Не удалось декодировать JWT токен", ex);
//            }
//        };
//    }
}