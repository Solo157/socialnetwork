package org.counter.repository;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Событие из outbox-таблицы message_outbox, которое нужно отправить в Kafka.
 *
 * Жизненный цикл: NEW (создано в транзакции вместе с основным действием)
 * -> SENT (доставлено в Kafka). Записи не удаляются.
 * id записи одновременно eventId события — ключ идемпотентности.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogMessageOutbox {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_SENT = "SENT";

    /** Id записи outbox, одновременно eventId события в Kafka. */
    private String id;

    /** Связанное сообщение диалога. */
    private String messageId;

    /** Пользователь-получатель; у него меняется счётчик непрочитанных. */
    private String receiverId;

    /** Диалог, к которому относится событие. */
    private String dialogId;

    /** Отправитель сообщения. */
    private String senderId;

    /** Тип события: "message.created" или "message.read". */
    private String eventType;

    /** Статус публикации: NEW (ждёт отправки) или SENT (отправлено в Kafka). */
    private String status;

    /** Момент создания события. */
    private LocalDateTime createdAt;

    /** Момент отправки в Kafka; заполняется при переходе в SENT. */
    private LocalDateTime sentAt;

}
