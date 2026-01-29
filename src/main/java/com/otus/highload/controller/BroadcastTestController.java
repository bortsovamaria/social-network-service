package com.otus.highload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/test/broadcast")
@RequiredArgsConstructor
public class BroadcastTestController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/send")
    public String sendBroadcast() {
        Map<String, Object> message = Map.of(
            "test", true,
            "message", "Test broadcast to /topic/feed",
            "time", new Date().toString()
        );
        
        messagingTemplate.convertAndSend("/topic/feed", message);
        
        return "Broadcast sent to /topic/feed";
    }
}