package org.counter.model;

import lombok.*;

import java.io.Serializable;

/**
 * Событие в Kafka для counter-service.
 * Ключ Kafka-сообщения = recipientId (одна партиция на пользователя).
 * eventId — ключ идемпотентности: уникален для каждого события,
 * а не для каждого сообщения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String EVENT_MESSAGE_CREATED = "message.created";
    public static final String EVENT_MESSAGE_READ = "message.read";

    /** Уникальный id события (id записи outbox). Ключ идемпотентности. */
    private String eventId;

    /** Id сообщения диалога. */
    private String messageId;

    /** Пользователь-получатель. */
    private String recipientId;

    /** Отправитель. */
    private String senderId;

    /** Диалог. */
    private String dialogId;

    /** Тип: "message.created" или "message.read". */
    private String eventType;

    /** Момент создания события (ISO-8601). */
    private String createdAt;

}
