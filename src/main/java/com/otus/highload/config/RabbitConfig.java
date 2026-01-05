package com.otus.highload.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    public static final String FEED_UPDATE_QUEUE = "feed.update.queue";
    private static final String FEED_UPDATE_EXCHANGE = "feed.update.exchange";
    public static final String FEED_UPDATE_ROUTING_KEY = "feed.update";
    
    @Bean
    public Queue feedUpdateQueue() {
        return new Queue(FEED_UPDATE_QUEUE, true);
    }
    
    @Bean
    public DirectExchange feedExchange() {
        return new DirectExchange(FEED_UPDATE_EXCHANGE);
    }
    
    @Bean
    public Binding feedUpdateBinding() {
        return BindingBuilder.bind(feedUpdateQueue())
                .to(feedExchange())
                .with(FEED_UPDATE_ROUTING_KEY);
    }
}