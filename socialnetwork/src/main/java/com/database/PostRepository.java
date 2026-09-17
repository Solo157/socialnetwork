package com.database;

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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void save(PostEntity post) {
        String sql = """
                INSERT INTO posts (id, text, author_id, created_at)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                post.getId(),
                post.getText(),
                post.getAuthorId(),
                Timestamp.valueOf(post.getCreatedAt()));
    }

    public Optional<PostEntity> findById(String id) {
        String sql = "SELECT * FROM posts WHERE id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> toEntity(rs), id)
                .stream()
                .findFirst();
    }

    public void update(PostEntity post) {
        update(post.getId(), post.getText());
    }

    private void update(String id, String text) {
        String sql = "UPDATE posts SET text = ? WHERE id = ?";
        jdbcTemplate.update(sql, text, id);
    }

    public void delete(String id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<PostEntity> findPostsByAuthorIds(List<String> authorIds, int offset, int limit) {
        if (authorIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT * FROM posts WHERE author_id IN (:authorIds) ORDER BY created_at ASC LIMIT :limit OFFSET :offset";

        MapSqlParameterSource params = new MapSqlParameterSource("authorIds", authorIds);
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> toEntity(rs));
    }

    private PostEntity toEntity(ResultSet rs) throws SQLException {
        return PostEntity.builder()
                .id(rs.getString(PostEntity.Fields.id))
                .text(rs.getString(PostEntity.Fields.text))
                .authorId(rs.getString("author_id"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

}
