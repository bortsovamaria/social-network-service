package com.otus.highload.repository;

import com.otus.highload.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;


    public User save(User user) {
        String sql = "INSERT INTO users (id, first_name, last_name, email, birthdate, biography, city, password, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getId());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getEmail());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(user.getBirthDate()));
            ps.setString(6, user.getBiography());
            ps.setString(7, user.getCity());
            ps.setString(8, user.getPassword());
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        });
        return user;
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id, first_name, last_name, email, birthdate, biography, city FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapperShort, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<List<User>> findByFirstNameAndLastNameIgnoreCase(String firstName, String lastName) {
        String sql = "SELECT id, first_name, last_name, city, email, birthdate, biography FROM users " +
                "WHERE first_name LIKE ? AND last_name LIKE ?";
        return Optional.of(jdbcTemplate.query(sql, userRowMapperShort, firstName + "%", lastName + "%"));
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, email));
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        Timestamp birthdate = rs.getTimestamp("birthdate");
        if (birthdate != null) {
            user.setBirthDate(birthdate.toLocalDateTime());
        }
        user.setBiography(rs.getString("biography"));
        user.setCity(rs.getString("city"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    };

    private final RowMapper<User> userRowMapperShort = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        Timestamp birthdate = rs.getTimestamp("birthdate");
        if (birthdate != null) {
            user.setBirthDate(birthdate.toLocalDateTime());
        }
        user.setBiography(rs.getString("biography"));
        user.setCity(rs.getString("city"));
        user.setEmail(rs.getString("email"));
        return user;
    };


}
