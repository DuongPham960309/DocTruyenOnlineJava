package com.example.server.config;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of("images").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/assets/*").addResourceLocations(location);
    }
}