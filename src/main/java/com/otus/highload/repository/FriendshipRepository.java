package com.otus.highload.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendshipRepository {

    private final JdbcTemplate jdbcTemplate;

    public void addFriend(String userId, String friendId) {
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, userId);
            ps.setString(2, friendId);
            return ps;
        });
        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<String> getFriendIds(String userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    public void deleteFriend(String userId, String friendId) {
        String sql = "DELETE friendship where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<String> getFollowersId(String userId) {
        String sql = "SELECT user_id FROM friendship WHERE friend_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }
}
