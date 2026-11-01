package com.example.server.shared.middleware;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter implements Filter {
    private record Hit(long resetTime, int count) {}

    private final long windowMs;
    private final int max;
    private final String message;
    private final ConcurrentHashMap<String, Hit> hits = new ConcurrentHashMap<>();

    public RateLimitFilter(long windowMs, int max, String message) {
        this.windowMs = windowMs;
        this.max = max;
        this.message = message;
    }

    public void doFilter(
        ServletRequest req, ServletResponse res, FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        long now = System.currentTimeMillis();

        if (hits.size() > 10000) {
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

        chain.doFilter(req, res);
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