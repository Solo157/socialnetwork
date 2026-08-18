package com.configuration;

import com.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Что JwtFilter делает: предоставляет Authentication текущего пользователя для контекста spring security.
 * Выполняется один раз для каждого HTTP-запроса.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Задача метода - из JWT создать Authentication для контекста.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("URI = " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Это объект, который описывает текущего пользователя.
                Authentication authentication = jwtService.getAuthentication(token);
                // Spring хранит информацию о пользователе в ThreadLocal.
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // если не удалось получить токен и создать Authentication, то отдаем запрос без изменений
        filterChain.doFilter(request, response);
    }
}
