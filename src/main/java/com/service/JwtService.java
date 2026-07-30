package com.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Service
public class JwtService {

    // закладываем простой секрет для содания ключа для работы с токеном
    private static final String SECRET = "SocialNetworkSecretKeyForJwtTokenGeneration2024!";
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24;

    private final Key key;

    public JwtService() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * Генерация токена, его подпись.
     */
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлечение пользователя из токена.
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Получить объект аутентификации для спринг секьюрити. Нужен, чтобы спринг понимал, что такой-то пользователь
     * сейчас аутентифицирован, т.е. находится в контексте спринга.
     */
    public Authentication getAuthentication(String token) {
        return new UsernamePasswordAuthenticationToken(
                getUserIdFromToken(token),
                null,
                List.of()
        );
    }

}
