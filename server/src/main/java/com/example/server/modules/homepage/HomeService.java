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
        futures.forEach((sectionName, future) -> data.put(sectionName, future.join()));

        data.put("leftOfShortNovel", ((List<Map<String, String>>) data.get("leftOfShortNovel")).getFirst());
        formatTrendNovels(data);

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
            data.put(sectionName, future.join());

            if (data.get(sectionName) == null) {
                lastUpdatedTime.put(sectionName, updatedTime.get(sectionName));
            }
        });

        if (!Objects.equals(updatedTime.get("leftOfShortNovel"), lastUpdatedTime.get("leftOfShortNovel"))) {
            data.put("leftOfShortNovel", ((List<Map<String, String>>) data.get("leftOfShortNovel")).getFirst());
        }

        if (!Objects.equals(updatedTime.get("trendNovelsInMonth"), lastUpdatedTime.get("trendNovelsInMonth"))) {
            formatTrendNovels(data);
        }

        return new HomePageResponse(lastUpdatedTime, data);
    }

    private void formatTrendNovels(Map<String, Object> data) {
        List<Map<String, String>> list = (List<Map<String, String>>) data.get("trendNovelsInMonth");
        List<String> names = new ArrayList<>();

        for (Map<String, String> map : list) {
            names.add(map.get("name"));
        }

        data.put("trendNovelsInMonth", names);
    }
}