package com.example.server;

import org.springframework.boot.SpringApplication;
import java.util.Map;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.server.config.EnvConfig;

@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServerApplication.class);
        String port = EnvConfig.PORT;

        app.setDefaultProperties(Map.of(
            "server.port", port,
            "spring.mvc.servlet.load-on-startup", "1"
        ));
        app.run(args);

        System.out.println("\u001B[32m Server started on localhost:" + port + "\u001B[0m");
    }
}