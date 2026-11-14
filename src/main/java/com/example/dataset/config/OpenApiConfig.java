package com.example.dataset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI datasetOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Dataset JSON Service API")
                .version("1.0")
                .description("Store JSON records in named datasets and query them with group-by and sort-by."));
    }
}