package com.api;

import com.dto.LoginRequest;
import com.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Получить токен по пользователю и паролю.
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);

        return Map.of("token", token);
    }

}
