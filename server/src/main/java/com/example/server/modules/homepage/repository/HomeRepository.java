package com.example.server.modules.homepage.repository;

import com.example.server.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class HomeRepository {
    private final DataSource dataSource = DatabaseConfig.getDataSource();

    private final Map<String, String> queryString = Map.of(
        "suggestedNovels", "SELECT novels.name, novels.before, novels.after, novels.image, suggested_novels.chapter FROM suggested_novels JOIN novels ON suggested_novels.name = novels.name ORDER BY id",
        "selectedTranslationNovels", "SELECT novels.name, novels.before, novels.after, novels.image, selected_translation_novels.chapter FROM selected_translation_novels JOIN novels ON selected_translation_novels.name = novels.name ORDER BY id",
        "updatedNovels", "SELECT name, novels.before, after, note, current_chapter AS chapter, last_time FROM novels WHERE first_time != last_time ORDER BY last_time DESC LIMIT 25",
        "fullNovels", "SELECT novels.name, novels.before, novels.after, novels.image, novels.current_chapter AS chapter FROM full_novels JOIN novels ON full_novels.name = novels.name ORDER BY id",
        "leftOfShortNovel", "SELECT image, title FROM short_novels WHERE short_novels.group = 'leftOfShortNovel'",
        "rightOfShortNovels", "SELECT image, title FROM short_novels WHERE short_novels.group = 'rightOfShortNovels' ORDER BY id",
        "reviewNovels", "SELECT image, title FROM review_novels ORDER BY id",
        "topGoodNovels", "SELECT name AS title, view FROM novels ORDER BY view DESC LIMIT 10",
        "newUpdateNovels", "SELECT name AS title, view FROM novels WHERE first_time = last_time ORDER BY first_time DESC LIMIT 10",
        "trendNovelsInMonth", "SELECT name FROM trend_novels_in_month ORDER BY id"
    );

    public Set<String> getSectionNames() {
        return queryString.keySet();
    }

    public void fetchLastUpdatedTime(Map<String, String> lastUpdatedTime) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM last_updated_time");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lastUpdatedTime.put(rs.getString("section"), rs.getString("time"));
            }
        } catch (Exception e) {
            System.err.println("\u001B[31m lastUpdatedTime: " + e.getMessage() + "\u001B[0m");
        }
    }

    public void fetchSectionData(Map<String, Object> data, String property) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryString.get(property));
             ResultSet rs = stmt.executeQuery()) {

            data.put(property, resultSetToList(rs));
        } catch (Exception e) {
            System.err.println("\u001B[31m " + property + ": " + e.getMessage() + "\u001B[0m");
        }
    }

    public void fetchSectionDataConditional(Map<String, String> updatedTime, String property, Map<String, Object> queriedData) {
        Map<String, String> lastUpdatedTime = (Map<String, String>) queriedData.get("lastUpdatedTime");
        Map<String, Object> data = (Map<String, Object>) queriedData.get("data");

        if (updatedTime != null && !Objects.equals(updatedTime.get(property), lastUpdatedTime.get(property))) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(queryString.get(property));
                 ResultSet rs = stmt.executeQuery()) {

                data.put(property, resultSetToList(rs));
            } catch (Exception e) {
                System.err.println("\u001B[31m " + property + ": " + e.getMessage() + "\u001B[0m");
                lastUpdatedTime.put(property, updatedTime.get(property));
            }
        }
    }

    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }
}