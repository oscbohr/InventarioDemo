package com.inventory.activos.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos del contrato OpenAPI 3; Swagger UI en /swagger-ui.html y JSON en /v1/api-docs. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("InventoryDemo API")
                .description("API REST para gestionar artículos del inventario y calcular su depreciación anual")
                .version("0.0.1"));
    }
}
