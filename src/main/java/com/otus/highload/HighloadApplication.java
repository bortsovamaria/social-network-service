package com.otus.highload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableWebSocket
@SpringBootApplication
public class HighloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighloadApplication.class, args);
    }

}
