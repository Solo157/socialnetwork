package com.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DialogRepository {

    private final DataSource dataSource;

    public void save(DialogEntity dialog) {
        String sql = """
                INSERT INTO dialogs (id, user1_id, user2_id, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, dialog.getId());
            ps.setString(2, dialog.getUser1Id());
            ps.setString(3, dialog.getUser2Id());
            ps.setTimestamp(4, Timestamp.valueOf(dialog.getCreatedAt()));

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<DialogEntity> findByParticipants(String userId1, String userId2) {
        String sql = """
                SELECT * FROM dialogs
                WHERE (user1_id = ? AND user2_id = ?)
                   OR (user1_id = ? AND user2_id = ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, userId1);
            ps.setString(2, userId2);
            ps.setString(3, userId2);
            ps.setString(4, userId1);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(DialogEntity.builder()
                        .id(rs.getString("id"))
                        .user1Id(rs.getString("user1_id"))
                        .user2Id(rs.getString("user2_id"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
