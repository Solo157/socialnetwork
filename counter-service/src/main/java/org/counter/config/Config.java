package org.counter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Общая конфигурация.
 */
@Configuration
public class Config {

    /**
     * Объявление ObjectMapper бина.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Создание топика и партиций.
     */
    @Bean
    public NewTopic counterEventsTopic() {
        return TopicBuilder
                .name("counter-events")
                .partitions(2)
                .replicas(1)
                .build();
    }

}
