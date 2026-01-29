package com.otus.highload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final RabbitTemplate rabbitTemplate;

    @MessageMapping("/test")
    public void testMessage() {
        rabbitTemplate.convertAndSend("/topic/feed",
            Map.of("test", true, "time", new Date()));
    }
}