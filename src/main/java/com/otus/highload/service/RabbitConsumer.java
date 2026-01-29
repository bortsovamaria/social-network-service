package com.otus.highload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.otus.highload.config.RabbitConfig.FEED_CELEBRITY_QUEUE;
import static com.otus.highload.config.RabbitConfig.FEED_REGULAR_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitConsumer {

    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private final FriendshipService friendshipService;

    @RabbitListener(queues = FEED_REGULAR_QUEUE)
    public void processRegularPost(String message) {
        try {
            RabbitService.PostMessage postMessage = objectMapper.readValue(message, RabbitService.PostMessage.class);
            log.info("Processing regular post: {}", postMessage.getPost().getId());
            invalidateFeedCache(postMessage.getFollowerIds());
        } catch (Exception e) {
            log.error("Error processing regular post", e);
        }
    }

    @RabbitListener(queues = FEED_CELEBRITY_QUEUE)
    public void processCelebrityPost(String message) {
        try {
            RabbitService.PostMessage postMessage = objectMapper.readValue(message, RabbitService.PostMessage.class);
            log.info("Processing celebrity post: {} ({} followers)",
                    postMessage.getPost().getId(), postMessage.getFollowerIds().size());
            invalidateFeedCacheBatch(postMessage.getFollowerIds());
        } catch (Exception e) {
            log.error("Error processing celebrity post", e);
        }
    }

    private void invalidateFeedCache(List<String> userIds) {
        Cache feedCache = cacheManager.getCache("feed");
        if (feedCache != null) {
            for (String userId : userIds) {
                feedCache.evict(userId);
            }
            log.debug("Invalidated feed cache for {} users", userIds.size());
        }
    }

    private void invalidateFeedCacheBatch(List<String> userIds) {
        Cache feedCache = cacheManager.getCache("feed");
        if (feedCache != null) {
            int batchSize = 100;
            int processed = 0;
            
            for (String userId : userIds) {
                feedCache.evict(userId);
                processed++;
                
                if (processed % batchSize == 0) {
                    log.debug("Processed batch of {} users", batchSize);
                }
            }
            log.info("Invalidated feed cache for {} users (batch processing)", userIds.size());
        }
    }
    
    public boolean isCelebrity(String userId) {
        int followerCount = friendshipService.getFollowerIds(userId).size();
        return followerCount > 1000;
    }
}