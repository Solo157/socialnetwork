
package com.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Создать нового пользователя.
     */
    public void save(UserEntity user) {
        String sql = """
                INSERT INTO users(
                    id,
                    firstName,
                    secondName,
                    birthdate,
                    biography,
                    city,
                    passwordHash,
                    friends
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                user.getId(),
                user.getFirstName(),
                user.getSecondName(),
                Date.valueOf(user.getBirthdate()),
                user.getBiography(),
                user.getCity(),
                user.getPasswordHash(),
                user.getFriends() != null ? toJson(user.getFriends()) : null);
    }

    public Optional<UserEntity> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> toEntity(rs), id)
                .stream()
                .findFirst();
    }

    public void addFriend(String userId, String friendId) {
        List<String> friendsFromDb = loadFriendsFromDb(userId);
        // если нет друзей, то инициируем новый список
        if (friendsFromDb == null) {
            friendsFromDb = new ArrayList<>();
        }
        // если друга нет, то добавляем в список друзей
        if (!friendsFromDb.contains(friendId)) {
            friendsFromDb.add(friendId);
        }

        String sql = "UPDATE users SET friends = ? WHERE id = ?";
        jdbcTemplate.update(sql, toJson(friendsFromDb), userId);
    }

    public void removeFriend(String userId, String friendId) {
        List<String> friendsFromDb = loadFriendsFromDb(userId);
        if (friendsFromDb != null) {
            friendsFromDb.removeIf(f -> f.equals(friendId));
        }

        String friendsForUpdate = friendsFromDb != null && !friendsFromDb.isEmpty() ? toJson(friendsFromDb) : null;

        String sql = "UPDATE users SET friends = ? WHERE id = ?";
        jdbcTemplate.update(sql, friendsForUpdate, userId);
    }

    public List<String> findUsersWithFriend(String friendId) {
        String sql = "SELECT id FROM users WHERE friends LIKE ?";
        return jdbcTemplate.queryForList(sql, String.class, "%\"" + friendId + "\"%");
    }

    public List<UserEntity> search(String firstName, String secondName) {
        String sql = """
                SELECT *
                FROM users
                WHERE firstName LIKE ?
                  AND secondName LIKE ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> UserEntity.builder()
                .id(rs.getString(UserEntity.Fields.id))
                .firstName(rs.getString(UserEntity.Fields.firstName))
                .secondName(rs.getString(UserEntity.Fields.secondName))
                .city(rs.getString(UserEntity.Fields.city))
                .build(), firstName + "%", secondName + "%");
    }

    private List<String> loadFriendsFromDb(String userId) {
        String sql = "SELECT friends FROM users WHERE id = ?";

        List<String> rows = jdbcTemplate.queryForList(sql, String.class, userId);
        if (rows.isEmpty()) {
            return null;
        }
        return parseFriends(rows.get(0));
    }

    private UserEntity toEntity(ResultSet rs) throws SQLException {
        return UserEntity.builder()
                .id(rs.getString(UserEntity.Fields.id))
                .firstName(rs.getString(UserEntity.Fields.firstName))
                .secondName(rs.getString(UserEntity.Fields.secondName))
                .birthdate(rs.getDate(UserEntity.Fields.birthdate).toLocalDate())
                .biography(rs.getString(UserEntity.Fields.biography))
                .city(rs.getString(UserEntity.Fields.city))
                .passwordHash(rs.getString(UserEntity.Fields.passwordHash))
                .friends(parseFriends(rs.getString(UserEntity.Fields.friends)))
                .build();
    }

    private List<String> parseFriends(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String toJson(List<String> list) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
