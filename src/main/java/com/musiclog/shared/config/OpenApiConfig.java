package com.musiclog.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI musicLogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MusicLog API")
                        .version("0.1.0")
                        .description("""
                                API REST de MusicLog, una aplicación social para registrar, valorar y descubrir música.

                                Las rutas con el icono de candado requieren un token obtenido en `POST /api/auth/login`.
                                En Swagger UI, usa el botón **Authorize** e introduce únicamente el token JWT.
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer healthResponseSchemaCustomizer() {
        return openApi -> {
            Schema<?> healthResponse = openApi.getComponents().getSchemas().get("HealthResponse");
            if (healthResponse != null) {
                healthResponse.additionalProperties(false);
            }
        };
    }
}
