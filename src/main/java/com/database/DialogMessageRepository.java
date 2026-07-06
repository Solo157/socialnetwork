package com.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DialogMessageRepository {

    private final DataSource dataSource;

    public void save(DialogMessageEntity message) {
        String sql = """
                INSERT INTO dialog_messages (id, dialog_id, sender_id, receiver_id, text, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, message.getId());
            ps.setString(2, message.getDialogId());
            ps.setString(3, message.getSenderId());
            ps.setString(4, message.getReceiverId());
            ps.setString(5, message.getText());
            ps.setTimestamp(6, Timestamp.valueOf(message.getCreatedAt()));

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DialogMessageEntity> findByDialogId(String dialogId) {
        String sql = "SELECT * FROM dialog_messages WHERE dialog_id = ? ORDER BY created_at ASC";
        List<DialogMessageEntity> messages = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, dialogId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(DialogMessageEntity.builder()
                            .id(rs.getString("id"))
                            .dialogId(rs.getString("dialog_id"))
                            .senderId(rs.getString("sender_id"))
                            .receiverId(rs.getString("receiver_id"))
                            .text(rs.getString("text"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return messages;
    }

}
