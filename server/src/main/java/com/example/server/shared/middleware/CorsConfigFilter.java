package com.example.server.shared.middleware;

import com.example.server.config.EnvConfig;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfigFilter {
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
