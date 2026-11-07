package com.example.server.modules.homepage;

import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class HomeController extends HttpServlet {
    private final HomeService homeService = new HomeService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/data")
    public HomePageResponse getData() {
        return homeService.getHomePageData();
    }

    @PostMapping("/data")
    public ResponseEntity<HomePageResponse> postData(@RequestBody(required = false) Map<String, Object> raw) {
        Map<String, String> body = validateSections(raw);

        if (body == null) {
            return ResponseEntity.ok(null);
        }

        HomePageResponse result = homeService.postHomePageData(body);

        return ResponseEntity.ok(result);
    }

    private Map<String, String> validateSections(Map<String, Object> raw) {
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