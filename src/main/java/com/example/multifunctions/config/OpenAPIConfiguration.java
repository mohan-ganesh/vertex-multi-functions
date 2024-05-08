package com.example.multifunctions.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .addServersItem(new Server().url("/"));
    }

}