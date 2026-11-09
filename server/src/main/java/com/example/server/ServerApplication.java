package com.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import java.util.EnumSet;
import java.util.Map;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.server.config.EnvConfig;
import com.example.server.shared.middleware.VerifyRequestFilter;
import com.example.server.shared.middleware.RateLimitFilter;
import org.springframework.context.support.GenericApplicationContext;

@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServerApplication.class);

        app.setDefaultProperties(Map.of("spring.mvc.servlet.load-on-startup", "1"));

        app.addInitializers((GenericApplicationContext context) -> {
            context.registerBean(ServletWebServerFactory.class, () -> {
                return factory();
            });
        });

        app.run(args);
    }

    public static TomcatServletWebServerFactory factory() {
        int port = EnvConfig.PORT;

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(port);

        ServletContextInitializer initializer = servletContext -> {
            var rateLimitHolder = servletContext.addFilter("rateLimitFilter", new RateLimitFilter(
                15 * 60 * 1000L,
                10000,
                "Quá nhiều yêu cầu từ IP này, vui lòng thử lại sau 1 phút."
            ));
            rateLimitHolder.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST), true, "/*"
            );
        };

        factory.addInitializers(initializer);
        System.out.println("\u001B[32m Server started on localhost:" + port + "\u001B[0m");

        return factory;
    }
}