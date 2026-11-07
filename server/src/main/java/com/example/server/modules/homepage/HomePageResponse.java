package com.example.server.modules.homepage;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HomePageResponse {
    private Map<String, String> lastUpdatedTime;
    private Map<String, Object> data;
}