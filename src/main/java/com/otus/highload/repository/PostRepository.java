package com.otus.highload.repository;

import com.otus.highload.model.post.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public Post save(Post post) {
        String sql = "INSERT INTO posts (id, text, author_id, created_at) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                post.getId(),
                post.getText(),
                post.getAuthorId(),
                Timestamp.valueOf(post.getCreatedAt())
        );
        return post;
    }

    public void delete(String id) {
        String sql = "DELETE posts where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<Post> findById(String id) {
        String sql = "SELECT id, text, author_id, created_at FROM posts WHERE id = ?";
        List<Post> posts = jdbcTemplate.query(sql, postRowMapper, id);
        return posts.isEmpty() ? Optional.empty() : Optional.of(posts.get(0));
    }

    public Optional<List<Post>> findByAuthorId(String authorId) {
        String sql = "SELECT id, text, authorId, createdAt FROM posts " +
                "WHERE authorId = ?";
        return Optional.of(jdbcTemplate.query(sql, postRowMapper, authorId));
    }

    public List<Post> getFeed(List<String> friendIds) {
        String friendIdsStr = friendIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));

        String sql = "SELECT p.id, p.text, p.author_id, p.created_at " +
                "FROM posts p " +
                "WHERE p.author_id IN (" + friendIdsStr + ") " +
                "ORDER BY p.created_at DESC " +
                "LIMIT 1000";
        return jdbcTemplate.query(sql, postRowMapper);
    }

    private final RowMapper<Post> postRowMapper = (rs, rowNum) -> {
        Post post = new Post();
        post.setId(rs.getString("id"));
        post.setText(rs.getString("text"));
        post.setAuthorId(rs.getString("author_id"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return post;
    };
}
