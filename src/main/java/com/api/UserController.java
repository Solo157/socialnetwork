package com.api;

import com.dto.RegisterUserRequest;
import com.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер по работе с пользователями.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * Регистрация пользователя.
     */
    @PostMapping("/register")
    public Map<String, UUID> register(@RequestBody RegisterUserRequest request) {
        UUID userId = userService.register(request);

        return Map.of("userId", userId);
    }

    /**
     * Получить информацию по пользователю.
     */
    @GetMapping("/get/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

}
