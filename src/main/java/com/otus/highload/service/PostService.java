package com.otus.highload.service;

import com.otus.highload.mapper.PostMapper;
import com.otus.highload.model.post.Post;
import com.otus.highload.model.post.PostResponse;
import com.otus.highload.model.user.UserResponse;
import com.otus.highload.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FriendshipService friendshipService;
    private final WebSocketNotificationService webSocketService;
    private final RabbitService rabbitService;
    private final UserService userService;
    private final CacheManager cacheManager;
    private final FeedUpdateService feedUpdateService;
    private final PostMapper postMapper;

    @Transactional
    public Post createPostWs(Post post) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        post.setId(UUID.randomUUID().toString());
        post.setAuthorId(userId);
        post.setCreatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        List<String> followerIds = friendshipService.getFollowerIds(userId);
        rabbitService.sendPostToQueue(savedPost, List.copyOf(followerIds));
        webSocketService.notifyAll(savedPost);
        log.info("Post created: {}", savedPost.getId());
        return savedPost;
    }

    @CacheEvict(value = "feed", key = "#post.authorId")
    @Transactional
    public Post createPost(Post post) {
        post.setId(UUID.randomUUID().toString());
        post.setCreatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        feedUpdateService.notifyFeedUpdate(savedPost.getAuthorId());
        evictFollowersFeeds(post.getAuthorId());
        return post;
    }

    private void evictFollowersFeeds(String authorId) {
        List<String> followerIds = friendshipService.getFollowerIds(authorId);

        for (String followerId : followerIds) {
            Cache cache = cacheManager.getCache("feed");
            if (cache != null) {
                cache.evict(followerId);
            }
        }
    }

    @Transactional
    public Post update(Post post) {
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Post findById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public void delete(String id) {
        postRepository.delete(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "feed", key = "'user_feed:' + #email", unless = "#result == null || #result.isEmpty()")
    public List<PostResponse> getFeed(String id) {
        log.info("Get feed from DB for user: {}", id);
        UserResponse user = userService.getById(id);
        List<String> friendIds = friendshipService.getFriendIds(user.getId());
        if (friendIds.isEmpty()) {
            return List.of();
        }
        return postMapper.toResponse(postRepository.getFeed(friendIds));
    }
}
