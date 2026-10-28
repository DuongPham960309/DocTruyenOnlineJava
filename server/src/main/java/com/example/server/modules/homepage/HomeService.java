package com.example.server.modules.homepage;

import com.example.server.modules.homepage.dto.HomePageResponse;
import com.example.server.modules.homepage.repository.HomeRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HomeService {
    private final HomeRepository homeRepository = new HomeRepository();

    public HomePageResponse getHomePageData() {
        Map<String, String> lastUpdatedTime = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        homeRepository.fetchLastUpdatedTime(lastUpdatedTime);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String sectionName : homeRepository.getSectionNames()) {
            futures.add(CompletableFuture.runAsync(() -> homeRepository.fetchSectionData(data, sectionName)));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        formatData(data);

        return new HomePageResponse(lastUpdatedTime, data);
    }

    public HomePageResponse postHomePageData(Map<String, String> updatedTime) {
        Map<String, String> lastUpdatedTime = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> queriedDataWrapper = new HashMap<>();
        queriedDataWrapper.put("lastUpdatedTime", lastUpdatedTime);
        queriedDataWrapper.put("data", data);

        homeRepository.fetchLastUpdatedTime(lastUpdatedTime);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String sectionName : homeRepository.getSectionNames()) {
            futures.add(CompletableFuture.runAsync(() ->
                homeRepository.fetchSectionDataConditional(updatedTime, sectionName, queriedDataWrapper)
            ));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if (updatedTime != null && !Objects.equals(updatedTime.get("leftOfShortNovel"), lastUpdatedTime.get("leftOfShortNovel"))) {
            if (data.get("leftOfShortNovel") instanceof List<?> list && !list.isEmpty()) {
                data.put("leftOfShortNovel", list.get(0));
            }
        }

        if (updatedTime != null && !Objects.equals(updatedTime.get("trendNovelsInMonth"), lastUpdatedTime.get("trendNovelsInMonth"))) {
            formatTrendNovels(data);
        }

        return new HomePageResponse(lastUpdatedTime, data);
    }

    private void formatData(Map<String, Object> data) {
        if (data.get("leftOfShortNovel") instanceof List<?> list && !list.isEmpty()) {
            data.put("leftOfShortNovel", list.get(0));
        }
        formatTrendNovels(data);
    }

    private void formatTrendNovels(Map<String, Object> data) {
        if (data.get("trendNovelsInMonth") instanceof List<?> list) {
            List<String> names = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof Map<?, ?> map && map.containsKey("name")) {
                    names.add((String) map.get("name"));
                }
            }
            data.put("trendNovelsInMonth", names);
        }
    }
}