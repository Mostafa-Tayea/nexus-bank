package com.mostafa.nexus_bank.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter the JWT token obtained from the /api/v1/auth/login endpoint"
)
public class OpenApiConfig {

    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Nexus Bank API")
                        .description("Enterprise Banking System API - A comprehensive RESTful API for banking operations " +
                                "including user management, account operations, transactions, notifications, " +
                                "and audit logging.")
                        .version("v1")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Nexus Bank Development Team")
                                .email("dev@nexusbank.com")
                                .url("https://nexusbank.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("http://localhost:8080")
                        .description("Development server"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("https://api.nexusbank.com")
                        .description("Production server"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("Bearer Authentication"));
    }
}
