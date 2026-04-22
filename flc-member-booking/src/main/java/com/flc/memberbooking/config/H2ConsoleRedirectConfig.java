package com.flc.memberbooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ensures a request to /h2-console (no trailing slash) is redirected to /h2-console/
 * so the H2 console servlet (mapped to /h2-console/* by Spring Boot auto-configuration)
 * receives the request instead of the static resource handler.
 */
@Configuration
public class H2ConsoleRedirectConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect '/h2-console' to '/h2-console/' so the servlet mapping is matched.
        registry.addRedirectViewController("/h2-console", "/h2-console/");
    }
}
