package com.example.server.modules.homepage;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.server.shared.utils.JsonBodyReader;

public class HomeController extends HttpServlet {
    private final HomeService homeService = new HomeService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HomePageResponse result = homeService.getHomePageData();

        objectMapper.writeValue(resp.getWriter(), result);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> body = validateSections(req);

        if (body == null) {
            resp.setStatus(HttpServletResponse.SC_OK);

            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HomePageResponse result = homeService.postHomePageData(body);

        objectMapper.writeValue(resp.getWriter(), result);

        IO.println("end");
    }

    private Map<String, String> validateSections(HttpServletRequest req) {
        Map<String, Object> raw = JsonBodyReader.readObject(req);
        if (raw == null) {
            return null;
        }

        HomeRepository homeRepository = new HomeRepository();
        if (!raw.keySet().equals(homeRepository.getSectionNames())) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            if (!(e.getValue() instanceof String time)) {
                return null;
            }

            result.put(e.getKey(), time);
        }

        return result;
    }
}