package com.example.server.modules.homepage.controller;

import com.example.server.modules.homepage.dto.HomePageResponse;
import com.example.server.modules.homepage.service.HomeService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class HomeServlet extends HttpServlet {
    private final HomeService homeService = new HomeService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Đổi từ Map<String, Object> sang HomePageResponse
        HomePageResponse result = homeService.getHomePageData();
        objectMapper.writeValue(resp.getWriter(), result);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> body = objectMapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});

        // Đổi từ Map<String, Object> sang HomePageResponse
        HomePageResponse result = homeService.postHomePageData(body);

        objectMapper.writeValue(resp.getWriter(), result);
    }
}