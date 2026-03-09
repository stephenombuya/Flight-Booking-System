package com.flightbookingapp.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Springdoc OpenAPI 3 with JWT bearer security and descriptive metadata.
 * Access the UI at: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Booking System API")
                        .description("RESTful backend for searching, booking, and managing flights. " +
                                     "Authenticate via /api/v1/auth/login and include the returned " +
                                     "JWT in the Authorization header as: Bearer <token>")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Flight Booking Team")
                                .email("support@flightbooking.com"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste the JWT token obtained from /api/v1/auth/login")));
    }
}
