package com.example.server.modules.homepage;

import com.example.server.config.DatabaseConfig;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

public class HomeRepository {
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DatabaseConfig.getDataSource());

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

    public Map<String, String> fetchLastUpdatedTime() {
        Map<String, String> lastUpdatedTime = new HashMap<>();

        try {
            jdbcTemplate.query("SELECT * FROM last_updated_time", rs -> {
                lastUpdatedTime.put(rs.getString("section"), rs.getString("time"));
            });
        } catch (Exception e) {
            System.err.println("\u001B[31m lastUpdatedTime: " + e.getMessage() + "\u001B[0m");
        }

        return lastUpdatedTime;
    }

    public List<Map<String, Object>> fetchSectionData(String property) {
        try {
            return jdbcTemplate.queryForList(queryString.get(property));
        } catch (Exception e) {
            System.err.println("\u001B[31m " + property + ": " + e.getMessage() + "\u001B[0m");

            return null;
        }
    }
}