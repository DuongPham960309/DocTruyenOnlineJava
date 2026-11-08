package com.example.server.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.file.Path;
import java.util.List;
import org.springframework.core.Ordered;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(EnvConfig.CLIENT_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("Content-Type", "x-key"));

        CorsFilter cors = new CorsFilter(request -> config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(cors);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of("images").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/assets/*").addResourceLocations(location);
    }
}