package com.example.server.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;
import org.springframework.core.Ordered;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebConfig {
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
}