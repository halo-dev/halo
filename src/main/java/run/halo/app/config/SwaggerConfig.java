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
            .group("CoreAPI")
            .displayName("Core API")
            .pathsToMatch("/api/**")
            .build();
    }

    @Bean
    GroupedOpenApi extensionApi() {
        return GroupedOpenApi.builder()
            .group("ExtensionAPI")
            .displayName("Extension API")
            .pathsToMatch("/apis/**")
            .pathsToExclude("/apis/api.halo.run/**", "/apis/api.plugin.halo.run/**")
            .build();
    }

    @Bean
    GroupedOpenApi systemCustomApi() {
        return GroupedOpenApi.builder()
            .group("SystemCustomAPI")
            .displayName("System Custom API")
            .pathsToMatch("/apis/api.halo.run/**")
            .build();
    }

    @Bean
    GroupedOpenApi pluginCustomApi() {
        return GroupedOpenApi.builder()
            .group("PluginCustomAPI")
            .displayName("Plugin Custom API")
            .pathsToMatch("/apis/api.plugin.halo.run/**")
            .build();
    }

}
