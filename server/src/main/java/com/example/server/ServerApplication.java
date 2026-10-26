package com.example.server;

import com.example.server.modules.homepage.controller.HomeServlet;
import com.example.server.shared.middleware.CorsFilter;
import com.example.server.shared.middleware.VerifyRequestFilter;
import jakarta.servlet.DispatcherType;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import java.io.File;
import java.util.EnumSet;

public class ServerApplication {

    public static void main(String[] args) {
        int port = 4000;

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(port);

        // Map đường dẫn /assets vào thư mục images thông qua ContextCustomizer
        factory.addContextCustomizers(context -> {
            StandardRoot resources = new StandardRoot(context);
            String absolutePath = new File("images").getAbsolutePath();

            resources.addPreResources(new DirResourceSet(
                resources,
                "/assets",
                absolutePath,
                "/"
            ));
            context.setResources(resources);
        });

        ServletContextInitializer initializer = servletContext -> {
            // 1. Đăng ký HomeServlet
            var homeServletHolder = servletContext.addServlet("homeServlet", new HomeServlet());
            homeServletHolder.addMapping("/data");

            // 2. Đăng ký CorsFilter
            var corsFilterHolder = servletContext.addFilter("corsFilter", new CorsFilter());
            corsFilterHolder.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

            // 3. Đăng ký VerifyRequestFilter
            var filterHolder = servletContext.addFilter("verifyRequestFilter", new VerifyRequestFilter());
            filterHolder.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

            // 4. Phục vụ Static Files qua DefaultServlet
            var staticServletHolder = servletContext.addServlet("default", "org.apache.catalina.servlets.DefaultServlet");
            staticServletHolder.setInitParameter("dirAllowed", "false");
            staticServletHolder.addMapping("/assets/*");
        };

        WebServer webServer = factory.getWebServer(initializer);
        webServer.start();
        System.out.println("\u001B[32m Server started on localhost:" + port + "\u001B[0m");
    }
}