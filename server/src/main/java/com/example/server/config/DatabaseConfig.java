package com.example.server.config;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {
    public static final DataSource DATA_SOURCE = createDataSource();

    private static HikariDataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(
                "jdbc:mysql://" + EnvConfig.MYSQL_HOST + ":" + EnvConfig.MYSQL_PORT +
                    "/" + EnvConfig.MYSQL_DATABASE + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            );
            config.setUsername(EnvConfig.MYSQL_USER);
            config.setPassword(EnvConfig.MYSQL_PASSWORD);
            config.setMaximumPoolSize(10);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            HikariDataSource dataSource = new HikariDataSource(config);

            System.out.println("\u001B[32m MySQL connection established successfully!\u001B[0m");

            return dataSource;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to connect to MySQL Server! IS YOUR MYSQL SERVICE RUNNING?", e
            );
        }
    }
}