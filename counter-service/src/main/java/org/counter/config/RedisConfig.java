package org.counter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Конфигурация Redis для counter-service.
 *
 * Хранит Lua-скрипты для кэша диалогов (используются DialogCacheService).
 * Сам RedisTemplate не объявляем: Spring Boot автоматически
 * конфигурирует StringRedisTemplate, которым пользуется CounterCacheService.
 */
@Configuration
public class RedisConfig {

    /**
     * Бин скрипта на получение идентификатора диалога.
     */
    @Bean
    public RedisScript<String> getDialogIdScript() {
        return RedisScript.of(
                new ClassPathResource("redis/get_dialog_id.lua"),
                String.class
        );
    }

    /**
     * Бин скрипта на поиск или создание диалога.
     */
    @Bean
    public RedisScript<String> findOrCreateDialogScript() {
        return RedisScript.of(
                new ClassPathResource("redis/find_or_create_dialog.lua"),
                String.class
        );
    }

    /**
     * Бин скрипта на добавление сообщения в диалог.
     */
    @Bean
    public RedisScript<String> addMessageScript() {
        return RedisScript.of(
                new ClassPathResource("redis/add_message.lua"),
                String.class
        );
    }

    /**
     * Бин скрипта на получение всех сообщений диалога.
     */
    @Bean
    public RedisScript<List> getMessagesScript() {
        return RedisScript.of(
                new ClassPathResource("redis/get_messages.lua"),
                List.class
        );
    }

}
