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
    public String register(RegisterUserRequest request) {
        String id = UUID.randomUUID().toString();

        UserEntity user = UserEntity.builder()
                .id(id)
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .birthdate(request.getBirthdate())
                .biography(request.getBiography())
                .city(request.getCity())
                // хэшируем пароль, не должны его хранить в БД
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
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .secondName(user.getSecondName())
                .birthdate(user.getBirthdate())
                .biography(user.getBiography())
                .city(user.getCity())
                .build();
    }

    public List<UserResponse> search(String firstName, String secondName) {
        return userRepository.search(firstName, secondName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(UserEntity userEntity) {
        return UserResponse.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .secondName(userEntity.getSecondName())
//                .birthdate(userEntity.getBirthdate())
//                .biography(userEntity.getBiography())
                .city(userEntity.getCity())
                .build();
    }

    /**
     * Получить токен по идентификатору и паролю пользователя.
     * !!! Пока что метод реализован больше как заглушка !!!
     */
    public String login(LoginRequest request) {
        UserEntity user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String userPasswordHash = user.getPasswordHash();
        String requestPassword = request.getPassword();
        // сравниваем хэши из БД и из запроса, если совпадают, значит пароль верный.
        if (!userPasswordHash.equals(hashPassword(requestPassword))) {
            throw new RuntimeException("Invalid password");
        }

        return UUID.randomUUID().toString();
    }

    /**
     * Хэширование пароля.
     */
    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

}
