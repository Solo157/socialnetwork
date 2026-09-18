package org.counter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DialogRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(DialogEntity dialog) {
        String sql = """
                INSERT INTO dialogs (id, user1_id, user2_id, created_at)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                dialog.getId(),
                dialog.getUser1Id(),
                dialog.getUser2Id(),
                Timestamp.valueOf(dialog.getCreatedAt()));
    }

    public Optional<DialogEntity> findByParticipants(String userId1, String userId2) {
        String sql = """
                SELECT * FROM dialogs
                WHERE (user1_id = ? AND user2_id = ?)
                   OR (user1_id = ? AND user2_id = ?)
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> toEntity(rs), userId1, userId2, userId2, userId1)
                .stream()
                .findFirst();
    }

    private DialogEntity toEntity(ResultSet rs) throws SQLException {
        return DialogEntity.builder()
                .id(rs.getString("id"))
                .user1Id(rs.getString("user1_id"))
                .user2Id(rs.getString("user2_id"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

}
