package com.skillvault.backend.Security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI smartDocOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkillVault API")
                        .version("1.0")
                        .description("API for skill and certificate validations."));
    }
}
