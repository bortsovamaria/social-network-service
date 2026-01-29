package com.otus.highload.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    public static final String FEED_UPDATE_QUEUE = "feed.update.queue";
    public static final String FEED_UPDATE_EXCHANGE = "feed.update.exchange";
    public static final String FEED_UPDATE_ROUTING_KEY = "feed.update";

    public static final String FEED_EXCHANGE = "feed.exchange";
    public static final String FEED_REGULAR_QUEUE = "feed.regular.queue";
    public static final String FEED_CELEBRITY_QUEUE = "feed.celebrity.queue";
    public static final String FEED_REGULAR_ROUTING_KEY = "feed.regular";
    public static final String FEED_CELEBRITY_ROUTING_KEY = "feed.celebrity";
    
    @Bean
    public Queue feedUpdateQueue() {
        return new Queue(FEED_UPDATE_QUEUE, true);
    }
    
    @Bean
    public DirectExchange feedUpdateExchange() {
        return new DirectExchange(FEED_UPDATE_EXCHANGE);
    }
    
    @Bean
    public Binding feedUpdateBinding() {
        return BindingBuilder.bind(feedUpdateQueue())
                .to(feedUpdateExchange())
                .with(FEED_UPDATE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange feedExchange() {
        return new TopicExchange(FEED_EXCHANGE);
    }

    @Bean
    public Queue feedRegularQueue() {
        return QueueBuilder.durable(FEED_REGULAR_QUEUE)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue feedCelebrityQueue() {
        return QueueBuilder.durable(FEED_CELEBRITY_QUEUE)
                .withArgument("x-max-length", 50000)
                .build();
    }

    @Bean
    public Binding regularBinding() {
        return BindingBuilder.bind(feedRegularQueue())
                .to(feedExchange())
                .with(FEED_REGULAR_ROUTING_KEY);
    }

    @Bean
    public Binding celebrityBinding() {
        return BindingBuilder.bind(feedCelebrityQueue())
                .to(feedExchange())
                .with(FEED_CELEBRITY_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}