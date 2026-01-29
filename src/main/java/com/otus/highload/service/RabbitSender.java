package com.otus.highload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otus.highload.model.post.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.otus.highload.config.RabbitConfig.FEED_EXCHANGE;


@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitSender {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendPostToQueue(Post post, Set<String> recipientIds) {
        try {
            PostMessage postMessage = new PostMessage(post, recipientIds);
            String messageJson = objectMapper.writeValueAsString(postMessage);
            Message message = MessageBuilder
                    .withBody(messageJson.getBytes())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();

            String routingKey = determineRoutingKey(post, recipientIds.size());
            rabbitTemplate.send(FEED_EXCHANGE, routingKey, message);
            log.debug("Post {} sent to RabbitMQ with routing key: {}", post.getId(), routingKey);
        } catch (Exception e) {
            log.error("Error sending post to RabbitMQ", e);
        }
    }

    private String determineRoutingKey(Post post, int followerCount) {
        if (followerCount > 1000) {
            return "feed.update.celebrity";
        }
        return "feed.update.regular";
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PostMessage {
        private Post post;
        private Set<String> recipientIds;
    }
}