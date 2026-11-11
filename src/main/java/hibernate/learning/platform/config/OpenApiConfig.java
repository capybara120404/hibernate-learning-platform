package hibernate.learning.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI learningPlatformOpenApi() {
        return new OpenAPI().info(new Info().title("Hibernate Learning Platform API").version("1.0.0")
                .description("REST API учебной платформы для управления курсами, заданиями, тестами и отзывами")
                .contact(new Contact().name("Learning Platform").email("platform@example.com")));
    }
}
