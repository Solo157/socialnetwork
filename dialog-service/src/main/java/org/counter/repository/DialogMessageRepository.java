package org.counter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import java.sql.*;
import java.util.*;

/**
 * Доступ к сообщениям диалогов (таблица dialog_messages).
 *
 * Соединения берём через DataSourceUtils.getConnection(), а не напрямую
 * из DataSource: если в потоке активна Spring-транзакция, будет возвращено
 * её соединение, и операции репозитория выполнятся в этой транзакции
 * (commit/rollback делает вызывающий сервис).
 */
@Repository
@RequiredArgsConstructor
public class DialogMessageRepository {

    private final DataSource dataSource;
    private final DialogRepository dialogRepository;

    /**
     * Сохраняет сообщение.
     */
    public void save(DialogMessageEntity message) {
        String sql = """
                INSERT INTO dialog_messages (id, dialog_id, sender_id, receiver_id, text, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, message.getId());
            ps.setString(2, message.getDialogId());
            ps.setString(3, message.getSenderId());
            ps.setString(4, message.getReceiverId());
            ps.setString(5, message.getText());
            ps.setTimestamp(6, Timestamp.valueOf(message.getCreatedAt()));

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * Все сообщения диалога по возрастанию времени создания.
     */
    public List<DialogMessageEntity> findByDialogId(String dialogId) {
        String sql = "SELECT * FROM dialog_messages WHERE dialog_id = ? ORDER BY created_at ASC";
        List<DialogMessageEntity> messages = new ArrayList<>();

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dialogId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(toEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        return messages;
    }

    /**
     * Все сообщения диалога между senderId и receiver (в обоих направлениях)
     * по возрастанию времени создания. Если диалог не существует — пустой список.
     */
    public List<DialogMessageEntity> findBySenderIdAndReceiver(String senderId, String receiver) {
        return dialogRepository.findByParticipants(senderId, receiver)
                .map(dialog -> findByDialogId(dialog.getId()))
                .orElseGet(ArrayList::new);
    }

    /**
     * Непрочитанные сообщения readerId из переданного списка id,
     * по возрастанию времени создания.
     *
     * FOR UPDATE блокирует найденные строки до конца текущей транзакции:
     * два конкурентных вызова не смогут пометить одни и те же сообщения
     * прочитанными дважды (иначе в outbox попали бы дубли message.read).
     */
    public List<DialogMessageEntity> findUnreadByIds(String readerId, List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = """
                SELECT * FROM dialog_messages
                WHERE id IN (%s)
                  AND receiver_id = ?
                  AND read_at IS NULL
                ORDER BY created_at ASC
                FOR UPDATE
                """.formatted(inPlaceholders(messageIds.size()));

        List<DialogMessageEntity> messages = new ArrayList<>();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindMessageIds(ps, messageIds);
            ps.setString(messageIds.size() + 1, readerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(toEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        return messages;
    }

    /**
     * Помечает сообщения прочитанными для readerId (read_at = NOW()).
     * Условие read_at IS NULL делает метод идемпотентным: повторная
     * пометка уже прочитанных сообщений ничего не меняет.
     */
    public void markRead(List<String> messageIds, String readerId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE dialog_messages
                SET read_at = NOW()
                WHERE id IN (%s)
                  AND receiver_id = ?
                  AND read_at IS NULL
                """.formatted(inPlaceholders(messageIds.size()));

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindMessageIds(ps, messageIds);
            ps.setString(messageIds.size() + 1, readerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * Заполняет плейсхолдеры IN-списка id-ми сообщений
     * (за ними в запросе идёт параметр readerId).
     */
    private void bindMessageIds(PreparedStatement ps, List<String> messageIds) throws SQLException {
        for (int i = 0; i < messageIds.size(); i++) {
            ps.setString(i + 1, messageIds.get(i));
        }
    }

    /**
     * Строка плейсхолдеров для IN-списка: 3 -> "?, ?, ?".
     */
    private static String inPlaceholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    /**
     * Преобразует строку dialog_messages в сущность.
     */
    private DialogMessageEntity toEntity(ResultSet rs) throws SQLException {
        return DialogMessageEntity.builder()
                .id(rs.getString("id"))
                .dialogId(rs.getString("dialog_id"))
                .senderId(rs.getString("sender_id"))
                .receiverId(rs.getString("receiver_id"))
                .text(rs.getString("text"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .readAt(rs.getTimestamp("read_at") == null
                        ? null
                        : rs.getTimestamp("read_at").toLocalDateTime())
                .build();
    }

}
