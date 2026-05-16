package com.service;

import com.api.UserResponse;
import com.database.UserEntity;
import com.database.UserRepository;
import com.dto.LoginRequest;
import com.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Зарегистрировать пользователя.
     */
    public UUID register(RegisterUserRequest request) {
        UUID id = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(id)
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .birthdate(request.getBirthdate())
                .biography(request.getBiography())
                .city(request.getCity())
                .passwordHash(hashPassword(request.getPassword()))
                .build();

        userRepository.save(user);

        return id;
    }

    /**
     * Получить информацию по пользователю.
     */
    public UserResponse getUser(UUID id) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .secondName(user.getSecondName())
                .birthdate(user.getBirthdate())
                .biography(user.getBiography())
                .city(user.getCity())
                .build();
    }

    /**
     * Получить токен по идентификатору и паролю пользователя.
     * !!! Пока что метод реализован больше как заглушка !!!
     */
    public String login(LoginRequest request) {
        UserEntity user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPasswordHash().equals(hashPassword(request.getPassword()))) {
            throw new RuntimeException("Invalid password");
        }

        return UUID.randomUUID().toString();
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}