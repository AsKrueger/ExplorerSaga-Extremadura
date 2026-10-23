package com.alonso.explorersaga.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI explorerSagaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ExplorerSaga API")
                        .version("v1")
                        .description("API REST del backend de ExplorerSaga para la gestión de lugares turísticos, categorías y fuentes de información."));
    }
}
