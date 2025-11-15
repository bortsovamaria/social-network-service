package com.otus.highload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class OpenApiConfig {
    
    @Bean
    @Primary
    public OpenAPI customOpenAPI() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream("/openapi.json");
        return mapper.readValue(inputStream, OpenAPI.class);
    }
}