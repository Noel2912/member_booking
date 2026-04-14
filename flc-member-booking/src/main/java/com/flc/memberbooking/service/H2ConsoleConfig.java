package com.flc.memberbooking.service;

import org.springframework.context.annotation.Configuration;

/**
 * H2 console configuration removed.
 *
 * Spring Boot (spring.h2.console.enabled=true) already auto-configures the
 * H2 web console in compatible environments. The previous manual registration
 * referenced org.h2.server.web.WebServlet which is compiled against the
 * javax.servlet API and causes a compile-time mismatch with Spring Boot 4's
 * Jakarta servlet API. To avoid that binding issue we rely on the built-in
 * Spring Boot H2 console support (configured in application.properties).
 */
@Configuration
public class H2ConsoleConfig {
    // Intentionally empty - use Spring Boot's auto-configuration for the H2 console.
}
