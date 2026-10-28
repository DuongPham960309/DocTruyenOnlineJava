package com.example.server;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.apache.catalina.webresources.StandardRoot;
import java.io.File;
import org.apache.catalina.webresources.DirResourceSet;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import java.util.EnumSet;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.server.WebServer;

import com.example.server.shared.middleware.CorsFilter;
import com.example.server.shared.middleware.VerifyRequestFilter;
import com.example.server.modules.homepage.HomeController;

public class ServerApplication {
    public static void main(String[] args) {
        int port = 4000;

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(port);

        factory.addContextCustomizers(context -> {
            StandardRoot resources = new StandardRoot(context);
            String absolutePath = new File("images").getAbsolutePath();

            resources.addPreResources(new DirResourceSet(
                resources, "/assets", absolutePath, "/"
            ));

            context.setResources(resources);
        });

        ServletContextInitializer initializer = servletContext -> {
            var corsFilterHolder = servletContext.addFilter("corsFilter", new CorsFilter());
            corsFilterHolder.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST), true, "/*"
            );

            var filterHolder = servletContext.addFilter("verifyRequestFilter", new VerifyRequestFilter());
            filterHolder.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

            var homeServletHolder = servletContext.addServlet("homeServlet", new HomeController());
            homeServletHolder.addMapping("/data");

            var staticServletHolder = servletContext.addServlet(
                "default", "org.apache.catalina.servlets.DefaultServlet"
            );
            staticServletHolder.setLoadOnStartup(1);
            staticServletHolder.addMapping("/assets/*");
        };

        WebServer webServer = factory.getWebServer(initializer);
        webServer.start();

        System.out.println("\u001B[32m Server started on localhost:" + port + "\u001B[0m");
    }
}