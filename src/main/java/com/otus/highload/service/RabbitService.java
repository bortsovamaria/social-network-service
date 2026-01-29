package com.otus.highload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otus.highload.model.post.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.otus.highload.config.RabbitConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendPostToQueue(Post post, List<String> followerIds) {
        try {
            PostMessage message = new PostMessage(post, followerIds);
            String jsonMessage = objectMapper.writeValueAsString(message);

            String routingKey = followerIds.size() > 1000 ?
                    FEED_CELEBRITY_ROUTING_KEY :
                    FEED_REGULAR_ROUTING_KEY;

            rabbitTemplate.convertAndSend(
                    FEED_EXCHANGE,
                    routingKey,
                    jsonMessage
            );

            log.info("Post {} sent to RabbitMQ with routing key: {} ({} followers)",
                    post.getId(), routingKey, followerIds.size());

        } catch (Exception e) {
            log.error("Error sending post to RabbitMQ", e);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PostMessage {
        private Post post;
        private List<String> followerIds;
    }
}