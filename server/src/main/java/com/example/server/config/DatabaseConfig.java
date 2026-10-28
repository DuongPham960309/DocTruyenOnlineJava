package com.example.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import javax.sql.DataSource;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/doc_truyen_online?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            config.setUsername("root");
            config.setPassword("123456");
            config.setMaximumPoolSize(10);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection()) {
                System.out.println("\u001B[32m MySQL connection established successfully!\u001B[0m");
            }
        } catch (Exception e) {
            System.err.println("\u001B[31m Error: Failed to connect to MySQL Server! IS YOUR MYSQL SERVICE RUNNING?\u001B[0m");
            System.err.println("\u001B[31m Details: " + e.getMessage() + "\u001B[0m");
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}