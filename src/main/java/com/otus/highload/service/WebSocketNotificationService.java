package com.otus.highload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otus.highload.model.post.Post;
import com.otus.highload.model.post.PostData;
import com.otus.highload.model.post.PostNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void notify(Post post, String userId) {
        try {
            PostNotification notification = new PostNotification(
                    "post.posted",
                    new PostData(post.getId(), post.getText(), post.getAuthorId())
            );
            String message = objectMapper.writeValueAsString(notification);
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/feed",
                    message
            );
            log.info("Broadcast WebSocket notification for post: {}", post.getId());
        } catch (Exception e) {
            log.error("Failed to send broadcast notification", e);
        }
    }
}