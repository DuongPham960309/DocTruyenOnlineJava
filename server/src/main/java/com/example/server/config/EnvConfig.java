package com.example.server.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    public static final String CLIENT_ORIGIN;

    public static final String HOST;
    public static final int PORT;

    public static final String MYSQL_HOST;
    public static final int MYSQL_PORT;
    public static final String MYSQL_USER;
    public static final String MYSQL_PASSWORD;
    public static final String MYSQL_DATABASE;

    static {
        String envName = System.getProperty("envName");
        Dotenv dotenv = Dotenv.configure().directory("./env").filename(".env." + envName).load();

        CLIENT_ORIGIN = dotenv.get("CLIENT_ORIGIN");

        HOST = dotenv.get("HOST");
        PORT = Integer.parseInt(dotenv.get("PORT"));

        MYSQL_HOST = dotenv.get("MYSQL_HOST");
        MYSQL_PORT = Integer.parseInt(dotenv.get("MYSQL_PORT"));
        MYSQL_USER = dotenv.get("MYSQL_USER");
        MYSQL_PASSWORD = dotenv.get("MYSQL_PASSWORD");
        MYSQL_DATABASE = dotenv.get("MYSQL_DATABASE");
    }
}
