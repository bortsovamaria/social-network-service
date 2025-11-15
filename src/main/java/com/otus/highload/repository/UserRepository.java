package com.otus.highload.repository;

import com.otus.highload.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;


    public User save(User user) {
        String sql = "INSERT INTO users (id, first_name, second_name, birthdate, biography, city) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getId());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getSecondName());
            ps.setDate(4, user.getBirthdate() != null ? java.sql.Date.valueOf(user.getBirthdate()) : null);
            ps.setString(5, user.getBiography());
            ps.setString(6, user.getCity());
            return ps;
        });
        return user;
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id, first_name, second_name, birthdate, biography, city FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public List<User> findByFirstNameAndLastNameIgnoreCase(String firstName, String lastName) {
        String sql = "SELECT id, first_name, second_name, birthdate, biography, city FROM users " +
                "WHERE LOWER(first_name) = LOWER(?) AND LOWER(second_name) = LOWER(?)";
        return jdbcTemplate.query(sql, userRowMapper,
                firstName, lastName);
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setSecondName(rs.getString("second_name"));
        Timestamp birthdate = rs.getTimestamp("birthdate");
        if (birthdate != null) {
            user.setBirthdate(birthdate.toLocalDateTime().toLocalDate());
        }
        user.setBiography(rs.getString("biography"));
        user.setCity(rs.getString("city"));
        return user;
    };


}
