package ru.admiralpashtet.reminder.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

/**
 * Утилитный класс для работы с JWT токеном.
 */
public class JwtUtil {
    private JwtUtil() {
    }

    public static boolean isJwt(String token) {
        try {
            Jwts.parser().build().parse(token);
        } catch (MalformedJwtException | IllegalArgumentException ignored) {
            return false;
        } catch (JwtException ignored) {
            return true;
        }
        return true;
    }
}
