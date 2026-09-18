package org.counter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.counter.model.CounterEvent;
import org.counter.repository.CounterRepository;
import org.counter.repository.UserMessageCounter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Логика подсчёта непрочитанных сообщений.
 *
 * Источники данных: PostgreSQL — источник истины (таблица
 * user_message_counters + журнал обработанных событий), Redis — кэш
 * для чтения по API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CounterService {

    private final CounterRepository counterRepository;
    private final CounterCacheService counterCacheService;

    /**
     * Обработка события из Kafka. Одна транзакция:
     * фиксация события по eventId (идемпотентность) + изменение
     * счётчика непрочитанных (created: +1, read: -1).
     * Если событие уже обработано, транзакция ничего не меняет.
     * Если транзакция падает, исключения доходят до listener,
     * который не шлёт ACK — Kafka перепоставит сообщение
     * (возможно, другому инстансу консьюмер-группы).
     * Обновление Redis — за пределами транзакции: см. updateRedisCounter().
     */
    @Transactional
    public void handleEvent(CounterEvent event) {
        if (event.getEventId() == null || event.getRecipientId() == null) {
            log.warn("Counter event without eventId/recipientId, skipping");
            return;
        }

        if (!counterRepository.tryRecordEvent(event.getEventId())) {
            log.info("Duplicate counter event {} for message {}", event.getEventId(), event.getMessageId());
            return;
        }

        if (CounterEvent.EVENT_MESSAGE_CREATED.equals(event.getEventType())) {
            counterRepository.incrementUnread(event.getRecipientId());
        } else if (CounterEvent.EVENT_MESSAGE_READ.equals(event.getEventType())) {
            counterRepository.decrementUnread(event.getRecipientId());
        } else {
            log.warn("Unknown counter event type {}", event.getEventType());
        }
    }

    /**
     * Обновляет Redis актуальным значением счётчика из БД.
     * Вызывается после commit транзакции handleEvent — и для нового
     * события, и для дубля (при дубле значение просто перечитывается).
     */
    public void updateRedisCounter(String userId) {
        UserMessageCounter counter = counterRepository.getCounter(userId);
        counterCacheService.setUnreadCount(userId, counter == null ? 0 : counter.getUnreadCount());
    }

    /**
     * Счётчик непрочитанных для API: сначала Redis, при промахе — БД
     * с последующим обновлением Redis.
     */
    public UserMessageCounter getCounter(String userId) {
        Long cached = counterCacheService.getUnreadCount(userId);
        if (cached != null) {
            return UserMessageCounter.builder()
                    .userId(userId)
                    .unreadCount(cached)
                    .build();
        }

        UserMessageCounter counter = counterRepository.getCounter(userId);
        long unreadCount = counter == null ? 0 : counter.getUnreadCount();
        counterCacheService.setUnreadCount(userId, unreadCount);

        return UserMessageCounter.builder()
                .userId(userId)
                .unreadCount(unreadCount)
                .lastMessageAt(counter == null ? null : counter.getLastMessageAt())
                .build();
    }

}
