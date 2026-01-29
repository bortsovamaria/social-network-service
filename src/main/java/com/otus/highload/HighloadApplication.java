package com.otus.highload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@EnableWebSocket
@SpringBootApplication
public class HighloadApplication {

	public static void main(String[] args) {
		System.out.println("🚀 STARTING SIMPLE WEB SOCKET TEST");
		SpringApplication.run(HighloadApplication.class, args);
	}

}
