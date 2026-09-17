package org.counter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Доступ к таблицам counter-service:
 * user_message_counters — счётчик непрочитанных на пользователя,
 * processed_counter_events — журнал обработанных событий (идемпотентность).
 */
@Repository
@RequiredArgsConstructor
public class CounterRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Помечает событие обработанным.
     * @return true, если событие зафиксировано впервые, false — это дубль
     *         (повторная доставка Kafka), и его надо пропустить.
     */
    public boolean tryRecordEvent(String eventId) {
        String sql = """
                INSERT INTO processed_counter_events (event_id, processed_at)
                VALUES (?, NOW())
                ON CONFLICT (event_id) DO NOTHING
                """;
        return jdbcTemplate.update(sql, eventId) > 0;
    }

    /**
     * Увеличивает счётчик непрочитанных сообщений пользователя (+1).
     */
    public void incrementUnread(String userId) {
        String sql = """
                INSERT INTO user_message_counters (user_id, unread_count, last_message_at)
                VALUES (?, 1, NOW())
                ON CONFLICT (user_id)
                DO UPDATE SET unread_count = user_message_counters.unread_count + 1,
                              last_message_at = NOW()
                """;
        jdbcTemplate.update(sql, userId);
    }

    /**
     * Уменьшает счётчик непрочитанных сообщений пользователя (-1),
     * не ниже нуля.
     */
    public void decrementUnread(String userId) {
        String sql = """
                UPDATE user_message_counters
                SET unread_count = GREATEST(unread_count - 1, 0)
                WHERE user_id = ?
                """;
        jdbcTemplate.update(sql, userId);
    }

    /**
     * Счётчик непрочитанных сообщений пользователя.
     */
    public UserMessageCounter getCounter(String userId) {
        String sql = "SELECT * FROM user_message_counters WHERE user_id = ?";

        var rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> UserMessageCounter.builder()
                        .userId(rs.getString("user_id"))
                        .unreadCount(rs.getLong("unread_count"))
                        .lastMessageAt(rs.getTimestamp("last_message_at") == null
                                ? null
                                : rs.getTimestamp("last_message_at").toLocalDateTime())
                        .build(),
                userId);

        return rows.isEmpty() ? null : rows.get(0);
    }

}
