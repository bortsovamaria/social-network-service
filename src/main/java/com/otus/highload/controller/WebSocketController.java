package com.otus.highload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/feed/subscribe")
    @SendToUser("/queue/feed")
    public String subscribeToFeed(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String userId = principal.getName();
        log.info("User {} subscribed to feed updates via WebSocket", userId);
        return "{\"status\":\"subscribed\",\"message\":\"You will receive real-time updates\"}";
    }

    @MessageMapping("/feed/unsubscribe")
    @SendToUser("/queue/feed")
    public String unsubscribeFromFeed(Principal principal) {
        String userId = principal.getName();
        log.info("User {} unsubscribed from feed updates", userId);
        return "{\"status\":\"unsubscribed\"}";
    }

    @MessageMapping("/ping")
    public void ping() {
        log.info("=== PING RECEIVED ===");
        log.info(">>> Controller method ping() invoked");
        String response = "{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}";
        log.info(">>> Sending to /topic/ping: {}", response);
        try {
            messagingTemplate.convertAndSend("/topic/ping", response);
            log.info(">>> Message sent successfully");
        } catch (Exception e) {
            log.error(">>> Error sending message: ", e);
        }
    }
}