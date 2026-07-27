package com.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final DataSource dataSource;

    public void save(PostEntity post) {
        String sql = """
                INSERT INTO posts (id, text, author_id, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, post.getId());
            ps.setString(2, post.getText());
            ps.setString(3, post.getAuthorId());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreatedAt()));

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<PostEntity> findById(String id) {
        String sql = "SELECT * FROM posts WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            PostEntity post = PostEntity.builder()
                    .id(rs.getString(PostEntity.Fields.id))
                    .text(rs.getString(PostEntity.Fields.text))
                    .authorId(rs.getString("author_id"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();

            return Optional.of(post);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(PostEntity post) {
        update(post.getId(), post.getText());
    }

    private void update(String id, String text) {
        String sql = "UPDATE posts SET text = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, text);
            ps.setString(2, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM posts WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PostEntity> findPostsByAuthorIds(List<String> authorIds, int offset, int limit) {
        List<PostEntity> posts = new ArrayList<>();

        if (authorIds.isEmpty()) {
            return posts;
        }

        // будет список ? авторов, затем их будем сетить конкретными значениями
        String placeholders = String.join(", ", Collections.nCopies(authorIds.size(), "?"));
        String sql = "SELECT * FROM posts WHERE author_id IN (" + placeholders + ") ORDER BY created_at ASC LIMIT ? OFFSET ?";

        int paramIndex = 1;
        int totalParams = authorIds.size() + 2;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // ? заменяются в sql на конкретные значения идентификаторов авторов
            for (String authorId : authorIds) {
                ps.setString(paramIndex++, authorId);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex++, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                posts.add(PostEntity.builder()
                        .id(rs.getString(PostEntity.Fields.id))
                        .text(rs.getString(PostEntity.Fields.text))
                        .authorId(rs.getString("author_id"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return posts;
    }

}
