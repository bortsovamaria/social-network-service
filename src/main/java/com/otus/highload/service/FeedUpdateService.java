package com.otus.highload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.otus.highload.config.RabbitConfig.FEED_UPDATE_QUEUE;
import static com.otus.highload.config.RabbitConfig.FEED_UPDATE_ROUTING_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedUpdateService {
    
    private final RabbitTemplate rabbitTemplate;

    public void notifyFeedUpdate(String authorId) {
        rabbitTemplate.convertAndSend(
                FEED_UPDATE_QUEUE,
                FEED_UPDATE_ROUTING_KEY,
                authorId);
        log.info("Published feed update for author: {}", authorId);
    }
}