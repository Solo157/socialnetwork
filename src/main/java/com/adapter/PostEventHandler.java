package com.adapter;

import com.configuration.RabbitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbit.PostCreatedEvent;
import com.rabbit.PostUpdatedEvent;
import com.service.FeedMaterializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.stereotype.Component;

/**
 * Обработчик ивентов из RabbitMQ для постов.
 */
@Component
@RequiredArgsConstructor
public class PostEventHandler {

    private final FeedMaterializationService materializationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.POST_CREATED_COMMAND_QUEUE)
    public void handlePostCreatedEvent(String messageBody) {
        System.out.println("Received message: " + messageBody);

        PostCreatedEvent postCreatedEvent = deserializeCommand(messageBody, PostCreatedEvent.class);
        if (postCreatedEvent == null) {
            System.out.println("Cannot deserialize to PostCreatedEvent");
            return;
        }

        materializationService.materializePostCreatedEvent(postCreatedEvent);
    }

    @RabbitListener(queues = RabbitConfig.POST_UPDATED_COMMAND_QUEUE)
    public void handlePostUpdatedEvent(String messageBody) {
        System.out.println("Received message: " + messageBody);

        PostUpdatedEvent postUpdatedEvent = deserializeCommand(messageBody, PostUpdatedEvent.class);
        if (postUpdatedEvent == null) {
            System.out.println("Cannot deserialize to PostUpdatedEvent");
            return;
        }

        materializationService.materializePostUpdatedEvent(postUpdatedEvent);
    }

    private <T> T deserializeCommand(String messageBody, Class<T> commandType) {
        try {
            return objectMapper.readValue(messageBody, commandType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
