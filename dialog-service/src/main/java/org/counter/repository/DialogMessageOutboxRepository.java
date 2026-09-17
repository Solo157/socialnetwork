package org.counter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import java.sql.*;
import java.util.*;

/**
 * Доступ к outbox-таблице message_outbox.
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

    private final DataSource dataSource;

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

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, outbox.getId());
            ps.setString(2, outbox.getMessageId());
            ps.setString(3, outbox.getReceiverId());
            ps.setString(4, outbox.getDialogId());
            ps.setString(5, outbox.getSenderId());
            ps.setString(6, outbox.getEventType());
            ps.setString(7, outbox.getStatus());
            ps.setTimestamp(8, Timestamp.valueOf(outbox.getCreatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
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

        List<DialogMessageOutbox> entries = new ArrayList<>();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(toEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        return entries;
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

        String sql = "UPDATE message_outbox SET status = 'SENT', sent_at = NOW() WHERE id IN ("
                + inPlaceholders(ids.size()) + ")";

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setString(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * Строка плейсхолдеров для IN-списка: 3 -> "?, ?, ?".
     */
    private static String inPlaceholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
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
