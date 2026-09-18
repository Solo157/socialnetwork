package org.counter.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.counter.annotation.DialogSqlEnabled;
import org.counter.model.CounterEvent;
import org.counter.repository.DialogMessageOutbox;
import org.counter.repository.DialogMessageOutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Паблишер outbox: периодически забирает записи message_outbox со статусом
 * NEW, отправляет их события в топик Kafka и помечает записи SENT.
 *
 * Ключ сообщения в Kafka — id получателя, поэтому все события одного
 * пользователя попадают в одну партицию и обрабатываются последовательно.
 *
 * Если сервис упадёт после отправки в Kafka, но до markSent, событие
 * уйдёт в Kafka повторно — это безопасно: counter-service идемпотентен
 * по eventId.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@DialogSqlEnabled
public class Scheduler {

    public static final String COUNTER_EVENTS_TOPIC = "counter-events";
    private static final int BATCH_SIZE = 100;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DialogMessageOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 100)
    public void publish() {
        List<DialogMessageOutbox> pending;
        try {
            pending = outboxRepository.findPending(BATCH_SIZE);
        } catch (Exception e) {
            // БД временно недоступна: просто пробуем в следующем цикле
            log.error("Outbox read failed", e);
            return;
        }
        if (pending.isEmpty()) {
            return;
        }

        List<String> sentIds = new ArrayList<>();
        for (DialogMessageOutbox entry : pending) {
            try {
                CounterEvent event = CounterEvent.builder()
                        .eventId(entry.getId())
                        .messageId(entry.getMessageId())
                        .recipientId(entry.getReceiverId())
                        .senderId(entry.getSenderId())
                        .dialogId(entry.getDialogId())
                        .eventType(entry.getEventType())
                        .createdAt(entry.getCreatedAt() == null
                                ? null : entry.getCreatedAt().toString())
                        .build();
                String payload = objectMapper.writeValueAsString(event);
                // ключ = получатель -> одна партиция на пользователя
                kafkaTemplate.send(COUNTER_EVENTS_TOPIC, entry.getReceiverId(), payload)
                        .get();
                sentIds.add(entry.getId());
            } catch (Exception e) {
                // запись остаётся NEW и будет взята в следующей итерации
                log.error("Failed to publish outbox entry {}", entry.getId(), e);
            }
        }

        if (!sentIds.isEmpty()) {
            try {
                outboxRepository.markSent(sentIds);
            } catch (Exception e) {
                // дубли в Kafka допустимы: counter-service идемпотентен по eventId
                log.error("Failed to mark outbox entries sent", e);
            }
        }
    }

}
