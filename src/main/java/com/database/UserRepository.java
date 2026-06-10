package com.database;

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
        // формируем insert на основе UserEntity
        String sql = """
                INSERT INTO users(
                    id,
                    firstName,
                    secondName,
                    birthdate,
                    biography,
                    city,
                    passwordHash
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             // Используем PreparedStatement, поэтому это защищает от SQL-инъекций
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, user.getId());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getSecondName());
            ps.setDate(4, Date.valueOf(user.getBirthdate()));
            ps.setString(5, user.getBiography());
            ps.setString(6, user.getCity());
            ps.setString(7, user.getPasswordHash());

            ps.executeUpdate(); // для модифицирующих запросов
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UserEntity> findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);

            ResultSet rs = ps.executeQuery(); // для запросов по выборке

            if (!rs.next()) {
                return Optional.empty();
            }

            UserEntity user = UserEntity.builder()
                    .id((String) rs.getObject(UserEntity.Fields.id))
                    .firstName(rs.getString(UserEntity.Fields.firstName))
                    .secondName(rs.getString(UserEntity.Fields.secondName))
                    .birthdate(rs.getDate(UserEntity.Fields.birthdate).toLocalDate())
                    .biography(rs.getString(UserEntity.Fields.biography))
                    .city(rs.getString(UserEntity.Fields.city))
                    .passwordHash(rs.getString(UserEntity.Fields.passwordHash))
                    .build();

            return Optional.of(user);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserEntity> search(String firstName, String secondName) {
        String sql = """
                SELECT *
                FROM users
                WHERE firstName LIKE ?
                  AND secondName LIKE ?
                ORDER BY id
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
//                        .birthdate(rs.getDate(UserEntity.Fields.birthdate).toLocalDate())
//                        .biography(rs.getString(UserEntity.Fields.biography))
                        .city(rs.getString(UserEntity.Fields.city))
//                        .passwordHash(rs.getString(UserEntity.Fields.passwordHash))
                        .build();

                users.add(user);
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
