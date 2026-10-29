package com.example.server.modules.homepage;

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

        data.put("leftOfShortNovel", ((List<Map<String, String>>) data.get("leftOfShortNovel")).getFirst());
        formatTrendNovels(data);

        return new HomePageResponse(lastUpdatedTime, data);
    }

    public HomePageResponse postHomePageData(Map<String, String> updatedTime) {
        Map<String, String> lastUpdatedTime = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("lastUpdatedTime", lastUpdatedTime);
        responsePayload.put("data", data);

        homeRepository.fetchLastUpdatedTime(lastUpdatedTime);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String sectionName : homeRepository.getSectionNames()) {
            futures.add(CompletableFuture.runAsync(() ->
                homeRepository.fetchSectionDataConditional(updatedTime, sectionName, responsePayload)
            ));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

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