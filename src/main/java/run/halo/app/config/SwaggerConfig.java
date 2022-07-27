package run.halo.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            // See https://swagger.io/docs/specification/authentication/ for more.
            .components(new Components()
                .addSecuritySchemes("BasicAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP).scheme("basic"))
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
            )
            .addSecurityItem(new SecurityRequirement().addList("BasicAuth").addList("BearerAuth"))
            .info(new Info().title("Halo Next API")
                .version("2.0.0"));
    }

    @Bean
    GroupedOpenApi extensionCoreApi() {
        return GroupedOpenApi.builder()
            .group("core-api")
            .displayName("Core APIs")
            .pathsToMatch("/api/**")
            .build();
    }

    @Bean
    GroupedOpenApi extensionApi() {
        return GroupedOpenApi.builder()
            .group("extension-api")
            .displayName("Extension APIs")
            .pathsToMatch("/apis/**")
            .pathsToExclude("/apis/api.halo.run/**", "/apis/plugin.api.halo.run/**")
            .build();
    }

    @Bean
    GroupedOpenApi systemCustomApi() {
        return GroupedOpenApi.builder()
            .group("core-custom-api")
            .displayName("Custom APIs in Core")
            .pathsToMatch("/apis/api.halo.run/**")
            .build();
    }

    @Bean
    GroupedOpenApi pluginCustomApi() {
        return GroupedOpenApi.builder()
            .group("plugin-custom-api")
            .displayName("Custom APIs in Plugin")
            .pathsToMatch("/apis/plugin.api.halo.run/**")
            .build();
    }

    @Bean
    GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
            .group("all-api")
            .displayName("All APIs")
            .pathsToMatch("/api/**", "/apis/**")
            .build();
    }

}
