package com.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * Общий топик постов. Через него происходит адресация в брокере RabbitMQ.
     */
    public static final String POST_EVENTS_TOPIC_EXCHANGE = "post.events";

    /**
     * Ключ для отправки ивента о том, что пост создан.
     */
    public static final String POST_CREATED_EVENT_KEY = "post.created.event.key";

    /**
     * Очередь для обработки команды о том, что пост создался.
     */
    public static final String POST_CREATED_COMMAND_QUEUE = "post.created.queue";

    /**
     * Ключ для отправки ивента о том, что пост обновлен.
     */
    public static final String POST_UPDATED_EVENT_KEY = "post.updated.event.key";

    /**
     * Очередь для обработки команды о том, что средства успешно сняты по заказу.
     */
    public static final String POST_UPDATED_COMMAND_QUEUE = "post.updated.queue";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(POST_EVENTS_TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue postCreatedQueue() {
        return new Queue(POST_CREATED_COMMAND_QUEUE, true, false, false);
    }

    @Bean
    public Queue postUpdatedQueue() {
        return new Queue(POST_UPDATED_COMMAND_QUEUE, true, false, false);
    }

    /**
     * Привязка очереди резервирования оплаты к обмену с ключем события о резервировании платежа.
     */
    @Bean
    public Binding bindPostCreatedQueueToExchange(Queue postCreatedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(postCreatedQueue)
                .to(topicExchange)
                .with(POST_CREATED_EVENT_KEY);
    }

    /**
     * Привязка очереди резервирования оплаты к обмену с ключем события о резервировании платежа.
     */
    @Bean
    public Binding bindPostUpdatedQueueToExchange(Queue postUpdatedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(postUpdatedQueue)
                .to(topicExchange)
                .with(POST_UPDATED_EVENT_KEY);
    }

}
