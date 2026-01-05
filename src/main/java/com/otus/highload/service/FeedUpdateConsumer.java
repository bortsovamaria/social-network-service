package com.otus.highload.service;

import com.otus.highload.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedUpdateConsumer {

    private final CacheManager cacheManager;
    private final FriendshipService friendshipService;

    @RabbitListener(queues = RabbitConfig.FEED_UPDATE_QUEUE)
    public void processFeedUpdate(String authorId) {
        log.info("Processing feed update for author: {}", authorId);
        try {
            List<String> followerIds = friendshipService.getFollowerIds(authorId);

            if (followerIds.isEmpty()) {
                return;
            }
            Cache feedCache = cacheManager.getCache("feed");
            if (feedCache != null) {
                for (String followerId : followerIds) {
                    feedCache.evict(followerId);
                }
            }
            log.info("Invalidated feed cache for {} followers", followerIds.size());
        } catch (Exception e) {
            log.error("Error processing feed update for author: {}", authorId, e);
        }
    }
}