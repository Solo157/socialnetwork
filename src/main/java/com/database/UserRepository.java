
package com.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DataSource dataSource;

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

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, user.getId());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getSecondName());
            ps.setDate(4, Date.valueOf(user.getBirthdate()));
            ps.setString(5, user.getBiography());
            ps.setString(6, user.getCity());
            ps.setString(7, user.getPasswordHash());
            ps.setString(8, user.getFriends() != null ? toJson(user.getFriends()) : null);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UserEntity> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            String userId = (String) rs.getObject(UserEntity.Fields.id);

            UserEntity user = UserEntity.builder()
                    .id(userId)
                    .firstName(rs.getString(UserEntity.Fields.firstName))
                    .secondName(rs.getString(UserEntity.Fields.secondName))
                    .birthdate(rs.getDate(UserEntity.Fields.birthdate).toLocalDate())
                    .biography(rs.getString(UserEntity.Fields.biography))
                    .city(rs.getString(UserEntity.Fields.city))
                    .passwordHash(rs.getString(UserEntity.Fields.passwordHash))
                    .friends(parseFriends(rs.getString(UserEntity.Fields.friends)))
                    .build();

            return Optional.of(user);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFriend(String userId, String friendId) {
        String sql = "UPDATE users SET friends = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            List<String> friendsFromDb = loadFriendsFromDb(userId);
            // если нет друзей, то инициируем новый список
            if (friendsFromDb == null) {
                friendsFromDb = new ArrayList<>();
            }
            // если друга нет, то добавляем в список друзей
            if (!friendsFromDb.contains(friendId)) {
                friendsFromDb.add(friendId);
            }

            ps.setString(1, toJson(friendsFromDb));
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFriend(String userId, String friendId) {
        String sql = "UPDATE users SET friends = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            List<String> friendsFromDb = loadFriendsFromDb(userId);
            if (friendsFromDb != null) {
                friendsFromDb.removeIf(f -> f.equals(friendId));
            }

            String friendsForUpdate = friendsFromDb != null && !friendsFromDb.isEmpty() ? toJson(friendsFromDb) : null;
            ps.setString(1, friendsForUpdate);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> findUsersWithFriend(String friendId) {
        String sql = "SELECT id FROM users WHERE friends LIKE ?";
        List<String> ids = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, "\"" + friendId + "\"");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ids;
    }

    public List<UserEntity> search(String firstName, String secondName) {
        String sql = """
                SELECT *
                FROM users
                WHERE firstName LIKE ?
                  AND secondName LIKE ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, firstName + "%");
            ps.setString(2, secondName + "%");

            ResultSet rs = ps.executeQuery();

            List<UserEntity> users = new ArrayList<>();

            while (rs.next()) {
                UserEntity user = UserEntity.builder()
                        .id((String) rs.getObject(UserEntity.Fields.id))
                        .firstName(rs.getString(UserEntity.Fields.firstName))
                        .secondName(rs.getString(UserEntity.Fields.secondName))
                        .city(rs.getString(UserEntity.Fields.city))
                        .build();
                users.add(user);
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> loadFriendsFromDb(String userId) {
        String sql = "SELECT friends FROM users WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseFriends(rs.getString("friends"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<String> parseFriends(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<String>>(){});
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
