package com.api;

import com.dto.RegisterUserRequest;
import com.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public Map<String, String> register(@RequestBody RegisterUserRequest request) {
        String userId = userService.register(request);

        return Map.of("userId", userId);
    }

    /**
     * Получить информацию по пользователю.
     */
    @GetMapping("/get/{id}")
    public UserResponse getUser(@PathVariable String id) {
        return userService.getUser(id);
    }

    /**
     * Поиск по фамилии и имени пользователя.
     * @return
     */
    @GetMapping("/search")
    public List<UserResponse> search(@RequestParam("first_name") String firstName,
                                     @RequestParam("last_name") String secondName) {
        return userService.search(firstName, secondName);
    }

    /**
     * Установить друга пользователя.
     */
    @PutMapping("/friend/set/{user_id}")
    public void setFriend(@PathVariable("user_id") String userId, HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("user_id");
        if (currentUserId == null) {
            throw new RuntimeException("Unauthorized");
        }

        userService.addFriend(currentUserId, userId);
    }

    /**
     * Удалить друга пользователя.
     */
    @PutMapping("/friend/delete/{user_id}")
    public void deleteFriend(@PathVariable("user_id") String userId, HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("user_id");
        if (currentUserId == null) {
            throw new RuntimeException("Unauthorized");
        }
        userService.removeFriend(currentUserId, userId);
    }

}
