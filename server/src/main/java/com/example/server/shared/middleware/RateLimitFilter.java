package com.example.server.shared.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitFilter {
    @Bean
    public FilterRegistrationBean<RateLimitCore> rateLimit() {
        FilterRegistrationBean<RateLimitCore> registration = new FilterRegistrationBean<>(new RateLimitCore(
            15 * 60 * 1000L,
            10000,
            "Quá nhiều yêu cầu từ IP này, vui lòng thử lại sau 15 phút."
        ));

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);

        return registration;
    }
}

class RateLimitCore extends OncePerRequestFilter {
    private record Hit(long resetTime, int count) {}

    private final long windowMs;
    private final int max;
    private final String message;
    private final ConcurrentHashMap<String, Hit> hits = new ConcurrentHashMap<>();

    public RateLimitCore(long windowMs, int max, String message) {
        this.windowMs = windowMs;
        this.max = max;
        this.message = message;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        long now = System.currentTimeMillis();

        if (hits.size() > 10_000) {
            hits.values().removeIf(hit -> hit.resetTime() <= now);
        }

        Hit hit = hits.compute(getClientIp(request), (key, current) ->
            (current == null || current.resetTime() <= now)
                ? new Hit(now + windowMs, 1)
                : new Hit(current.resetTime(), current.count() + 1)
        );

        long resetSeconds = (hit.resetTime() - now + 999) / 1000;

        response.setHeader("RateLimit-Limit", String.valueOf(max));
        response.setHeader("RateLimit-Remaining", String.valueOf(Math.max(0, max - hit.count())));
        response.setHeader("RateLimit-Reset", String.valueOf(resetSeconds));

        if (hit.count() > max) {
            response.setHeader("Retry-After", String.valueOf(resetSeconds));
            response.setStatus(429);
            response.setContentType("text/plain; charset=UTF-8");
            response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));

            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");

            return parts[parts.length - 1].trim();
        }

        return request.getRemoteAddr();
    }
}