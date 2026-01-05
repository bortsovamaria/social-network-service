package com.otus.highload.service;

import com.otus.highload.mapper.PostMapper;
import com.otus.highload.model.post.Post;
import com.otus.highload.model.post.PostResponse;
import com.otus.highload.model.user.User;
import com.otus.highload.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FriendshipService friendshipService;
    private final UserService userService;
    private final CacheManager cacheManager;
    private final FeedUpdateService feedUpdateService;
    private final PostMapper postMapper;


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
        Optional<Post> postOpt = postRepository.findById(post.getId());
        if (postOpt.isPresent()) {
            return postRepository.save(post);
        }
        throw new IllegalArgumentException();
    }

    @Transactional
    public void delete(String id) {
        Optional<Post> postOpt = postRepository.findById(id);
        postOpt.ifPresent(p -> {
            postRepository.delete(id);
            feedUpdateService.notifyFeedUpdate(p.getAuthorId());
        });
    }

    @Transactional(readOnly = true)
    public Post findById(String id) {
        return postRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<Post> findByAuthorId(String authorId) {
        return postRepository.findByAuthorId(authorId).orElseThrow();
    }

    @Cacheable(value = "feed", key = "'user_feed:' + #email", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<PostResponse> getFeed(String email) {
        log.info("Get feed from DB for user: {}", email);
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<String> friendIds = friendshipService.getFriendIds(user.get().getId());
        if (friendIds.isEmpty()) {
            return List.of();
        }
        return postMapper.toResponse(postRepository.getFeed(friendIds));
    }
}
