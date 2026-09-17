package org.counter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ к сообщениям диалогов (таблица dialog_messages).
 *
 * JdbcTemplate внутри использует DataSourceUtils.getConnection():
 * если в потоке активна Spring-транзакция, операции выполняются на её
 * соединении (commit/rollback делает вызывающий сервис).
 */
@Repository
@RequiredArgsConstructor
public class DialogMessageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DialogRepository dialogRepository;

    /**
     * Сохраняет сообщение.
     */
    public void save(DialogMessageEntity message) {
        String sql = """
                INSERT INTO dialog_messages (id, dialog_id, sender_id, receiver_id, text, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                message.getId(),
                message.getDialogId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getText(),
                Timestamp.valueOf(message.getCreatedAt()));
    }

    /**
     * Все сообщения диалога по возрастанию времени создания.
     */
    public List<DialogMessageEntity> findByDialogId(String dialogId) {
        String sql = "SELECT * FROM dialog_messages WHERE dialog_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> toEntity(rs), dialogId);
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
                WHERE id IN (:ids)
                  AND receiver_id = :readerId
                  AND read_at IS NULL
                ORDER BY created_at ASC
                FOR UPDATE
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("ids", messageIds);
        params.addValue("readerId", readerId);
        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> toEntity(rs));
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
                WHERE id IN (:ids)
                  AND receiver_id = :readerId
                  AND read_at IS NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("ids", messageIds);
        params.addValue("readerId", readerId);
        namedParameterJdbcTemplate.update(sql, params);
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
