package com.configuration;

import com.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // игнорируем эти два ендпоинта, к ним должен быть доступ без авторизации
        String uri = request.getRequestURI();
        if (uri.equals("/user/register") || uri.equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // для остальных запросов пытаемся получить пользователя из токена
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String userId = jwtService.getUserIdFromToken(token);
                request.setAttribute("user_id", userId);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // если не удалось получить, то отдаем запрос без изменений
        filterChain.doFilter(request, response);
    }
}
