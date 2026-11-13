package com.example.server.modules.homepage;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HomeService {
    private final HomeRepository homeRepository = new HomeRepository();

    public HomePageResponse getHomePageData() {
        Map<String, String> lastUpdatedTime = homeRepository.fetchLastUpdatedTime();
        Map<String, Object> data = new HashMap<>();

        Map<String, CompletableFuture<List<Map<String, Object>>>> futures = new HashMap<>();
        for (String sectionName : homeRepository.getSectionNames()) {
            futures.put(sectionName, CompletableFuture.supplyAsync(() -> homeRepository.fetchSectionData(sectionName)));
        }
        futures.forEach((sectionName, future) -> {
            data.put(sectionName, format(sectionName, future.join()));
        });

        return new HomePageResponse(lastUpdatedTime, data);
    }

    public HomePageResponse postHomePageData(Map<String, String> updatedTime) {
        Map<String, String> lastUpdatedTime = homeRepository.fetchLastUpdatedTime();
        Map<String, Object> data = new HashMap<>();

        Map<String, CompletableFuture<List<Map<String, Object>>>> futures = new HashMap<>();
        for (String sectionName : homeRepository.getSectionNames()) {
            futures.put(sectionName, CompletableFuture.supplyAsync(() -> {
                if (!Objects.equals(updatedTime.get(sectionName), lastUpdatedTime.get(sectionName))) {
                    return homeRepository.fetchSectionData(sectionName);
                } else {
                    return null;
                }
            }));
        }
        futures.forEach((sectionName, future) -> {
            data.put(sectionName, format(sectionName, future.join()));

            if (data.get(sectionName) == null) {
                lastUpdatedTime.put(sectionName, updatedTime.get(sectionName));
            }
        });

        return new HomePageResponse(lastUpdatedTime, data);
    }

    private Object format(String sectionName, List<Map<String, Object>> rows) {
        if (rows == null) {
            return null;
        }

        return switch (sectionName) {
            case "leftOfShortNovel" -> rows.getFirst();
            case "trendNovelsInMonth" -> rows.stream().map(row -> row.get("name")).toList();
            default -> rows;
        };
    }
}