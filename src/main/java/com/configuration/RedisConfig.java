package com.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Конфигурирует бин RedisTemplate для работы с Redis.
     * Шаблон настроен на JSON-сериализацию значений через Jackson,
     * что позволяет корректно сериализовать и десериализовать
     * произвольные Java-объекты.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // Настраиваем ObjectMapper: поддержка Java 8 date/time и полиморфная десериализация
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // поддержка LocalDateTime, LocalDate
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // даты как строки, не числа
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL // в JSON будет тип класса (@class), чтобы при чтении восстановить PostEntity, а не LinkedHashMap
        );

        // Создаём JSON-сериализатор с настроенным ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(mapper);

        // Инициализируем шаблон и привязываем подключение к Redis
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Ключи хранятся как строки
        template.setKeySerializer(new StringRedisSerializer());
        // Значения хранятся в формате JSON (List<PostEntity> и т.д.)
        template.setValueSerializer(serializer);

        // Завершаем конфигурацию: применяем сериализаторы
        template.afterPropertiesSet();

        return template;
    }

}
