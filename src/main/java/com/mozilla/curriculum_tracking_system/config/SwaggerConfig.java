package com.mozilla.curriculum_tracking_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public OpenAPI curriculumTrackingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Curriculum Tracking System API")
                        .description("API documentation for the Curriculum Tracking System...")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("support@curriculumtracking.com")
                                .url("https://github.com/mozilla/curriculum-tracking"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("Development server"),
                        new Server().url("https://curriculum-tracking-app.azurewebsites.net").description("Production server")
                ));
    }

}
