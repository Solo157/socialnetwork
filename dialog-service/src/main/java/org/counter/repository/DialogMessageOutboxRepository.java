package org.counter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Доступ к outbox-таблице message_outbox.
 *
 * JdbcTemplate внутри использует DataSourceUtils.getConnection():
 * если в потоке активна Spring-транзакция, операции выполняются на её
 * соединении (commit/rollback делает вызывающий сервис).
 *
 * Запись (событие) создаётся в той же транзакции, что и само действие
 * (создание сообщения, пометка прочтения), и проходит цикл NEW -> SENT:
 *  - NEW  — событие ждёт публикации в Kafka, его забирает Scheduler;
 *  - SENT — событие доставлено в Kafka.
 * Записи не удаляются (история событий остаётся в БД).
 * id записи одновременно eventId события в Kafka — ключ
 * идемпотентности в counter-service.
 */
@Repository
@RequiredArgsConstructor
public class DialogMessageOutboxRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Создаёт запись outbox. Статус берётся из сущности
     * (при создании всегда NEW).
     */
    public void insert(DialogMessageOutbox outbox) {
        String sql = """
                INSERT INTO message_outbox (id, message_id, receiver_id, dialog_id, sender_id,
                                            event_type, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                outbox.getId(),
                outbox.getMessageId(),
                outbox.getReceiverId(),
                outbox.getDialogId(),
                outbox.getSenderId(),
                outbox.getEventType(),
                outbox.getStatus(),
                Timestamp.valueOf(outbox.getCreatedAt()));
    }

    /**
     * События, ожидающие публикации, по времени создания (не больше limit).
     * Работает по частичному индексу idx_message_outbox_pending (status = 'NEW').
     */
    public List<DialogMessageOutbox> findPending(int limit) {
        String sql = """
                SELECT * FROM message_outbox
                WHERE status = 'NEW'
                ORDER BY created_at ASC, id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> toEntity(rs), limit);
    }

    /**
     * Помечает события отправленными (status = SENT, sent_at = NOW())
     * после успешной отправки в Kafka. Если сервис упадёт между отправкой
     * и пометкой, события уйдут в Kafka повторно — consumer их
     * проигнорирует как дубли по eventId.
     */
    public void markSent(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE message_outbox
                SET status = 'SENT', sent_at = NOW()
                WHERE id IN (:ids)
                """;

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("ids", ids));
    }

    /**
     * Преобразует строку message_outbox в сущность.
     */
    private DialogMessageOutbox toEntity(ResultSet rs) throws SQLException {
        return DialogMessageOutbox.builder()
                .id(rs.getString("id"))
                .messageId(rs.getString("message_id"))
                .receiverId(rs.getString("receiver_id"))
                .dialogId(rs.getString("dialog_id"))
                .senderId(rs.getString("sender_id"))
                .eventType(rs.getString("event_type"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .sentAt(rs.getTimestamp("sent_at") == null
                        ? null
                        : rs.getTimestamp("sent_at").toLocalDateTime())
                .build();
    }

}
