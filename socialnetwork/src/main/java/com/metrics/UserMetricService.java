package com.metrics;

import com.service.UserService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Класс метрики по добавленным камерам.
 */
@Service
public class UserMetricService {

    private final UserService userService;
    private final MeterRegistry meterRegistry;

    public UserMetricService(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.meterRegistry = meterRegistry;

        registerAddedUsersCount();
    }

    /**
     * Регистрация метрики по количеству зарегистрированных пользователей.
     * Будем получать количество прямо из базы. Пока что нет задания получать из кэша.
     */
    public void registerAddedUsersCount() {
        Gauge.builder("added_users_count",
                        userService,
                        cache -> userService.getUserCount())
                .description("Count of added users")
                .register(meterRegistry);
    }

}
