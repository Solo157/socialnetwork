package org.counter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis-кэш счётчика непрочитанных сообщений пользователя.
 * БД остаётся источником истины, Redis — кэш для операций чтения
 * по API. Ошибки Redis логируются и не прерывают обработку:
 * при недоступности Redis API и обработчик событий работают с БД.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CounterCacheService {

    private static final String KEY_PREFIX = "counter:unread:";

    private final StringRedisTemplate redisTemplate;

    private String key(String userId) {
        return KEY_PREFIX + userId;
    }

    /**
     * Текущее значение счётчика из Redis.
     * @return значение или null, если ключа нет (или Redis недоступен).
     */
    public Long getUnreadCount(String userId) {
        try {
            String value = redisTemplate.opsForValue().get(key(userId));
            return value == null ? null : Long.parseLong(value);
        } catch (Exception e) {
            log.error("Redis GET failed for user {}", userId, e);
            return null;
        }
    }

    /**
     * Записывает значение счётчика пользователя в Redis.
     */
    public void setUnreadCount(String userId, long count) {
        try {
            redisTemplate.opsForValue().set(key(userId), String.valueOf(count));
        } catch (Exception e) {
            log.error("Redis SET failed for user {}", userId, e);
        }
    }

}
