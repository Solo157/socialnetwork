package org.dialog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.*;

@Configuration
public class RedisConfig {

    /**
     * Конфигурирует бин RedisTemplate для работы с Redis.
     * Шаблон настроен на JSON-сериализацию значений через Jackson,
     * что позволяет корректно сериализовать и десериализовать
     * произвольные Java-объекты.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

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
