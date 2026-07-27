package com.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbit.PostCreatedEvent;
import com.rabbit.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.configuration.RabbitConfig.POST_CREATED_EVENT_KEY;
import static com.configuration.RabbitConfig.POST_EVENTS_TOPIC_EXCHANGE;
import static com.configuration.RabbitConfig.POST_UPDATED_EVENT_KEY;

/**
 * Отправляет ивенты постов в RabbitMQ.
 */
@Service
@RequiredArgsConstructor
public class PostEventSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendPostCreatedEvent(PostCreatedEvent event) {
        sendMessage(POST_CREATED_EVENT_KEY, event);
    }

    public void sendPostUpdatedEvent(PostUpdatedEvent event) {
        sendMessage(POST_UPDATED_EVENT_KEY, event);
    }

    private void sendMessage(String routingKey, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(POST_EVENTS_TOPIC_EXCHANGE, routingKey, payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
