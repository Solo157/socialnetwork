package org.counter.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.counter.model.CounterEvent;
import org.counter.service.CounterService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Консьюмер событий счётчика из Kafka.
 * Порядок операций:
 * 1) БД: транзакция (фиксация eventId + изменение счётчика), commit при возврате;
 * 2) Redis: актуальное значение из БД пишется в кэш;
 * 3) только после этого — ACK в Kafka.
 * При падении транзакции ACK не отправляется — Kafka перепоставит
 * сообщение (возможно, другому инстансу консьюмер-группы).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CounterKafkaListener {

    public static final String COUNTER_EVENTS_TOPIC = "counter-events";

    private final CounterService counterService;
    private final ObjectMapper objectMapper;

    /**
     * recipientId — это ключ Kafka-сообщения (id получателя); payload — JSON события.
     */
    @KafkaListener(topics = COUNTER_EVENTS_TOPIC, groupId = "counter-service")
    public void consume(String recipientId, String payload, Acknowledgment ack) {
        CounterEvent event;
        try {
            event = objectMapper.readValue(payload, CounterEvent.class);
        } catch (Exception e) {
            // некорректное сообщение не лечится повторной доставкой —
            // фиксируем и подтверждаем, чтобы не зациклиться
            log.error("Invalid counter event payload, skipping: {}", payload, e);
            ack.acknowledge();
            return;
        }

        if (event.getEventId() == null || event.getRecipientId() == null) {
            log.error("Counter event without eventId/recipientId, skipping: {}", payload);
            ack.acknowledge();
            return;
        }

        counterService.handleEvent(event);
        counterService.updateRedisCounter(event.getRecipientId());

        // событие обработано, кэш обновлён — подтверждаем доставку
        ack.acknowledge();
    }

}
