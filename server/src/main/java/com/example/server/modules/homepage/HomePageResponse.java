package com.example.server.modules.homepage;

import java.util.Map;

public class HomePageResponse {
    private Map<String, String> lastUpdatedTime;
    private Map<String, Object> data;

    public HomePageResponse() {
    }

    public HomePageResponse(Map<String, String> lastUpdatedTime, Map<String, Object> data) {
        this.lastUpdatedTime = lastUpdatedTime;
        this.data = data;
    }

    public Map<String, String> getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Map<String, String> lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}