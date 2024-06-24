package run.halo.app.config;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;

import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Set;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import run.halo.app.extension.router.JsonPatch;

@Configuration
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
@ConditionalOnWebApplication
public class SwaggerConfig {

    @Bean
    OpenAPI haloOpenApi(ObjectProvider<BuildProperties> buildPropertiesProvider,
        SpringDocConfigProperties docConfigProperties) {
        var buildProperties = buildPropertiesProvider.getIfAvailable();
        var version = "unknown";
        if (buildProperties != null) {
            version = buildProperties.getVersion();
        }
        return new OpenAPI()
            .specVersion(docConfigProperties.getSpecVersion())
            // See https://swagger.io/docs/specification/authentication/ for more.
            .components(new Components()
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic"))
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
            )
            .addSecurityItem(new SecurityRequirement()
                .addList("basicAuth")
                .addList("bearerAuth"))
            .info(new Info()
                .title("Halo")
                .version(version)
            );
    }

    @Bean
    GlobalOpenApiCustomizer openApiCustomizer() {
        return openApi -> JsonPatch.addSchema(openApi.getComponents());
    }
    //
    // @Bean
    // GroupedOpenApi extensionCoreApi() {
    //     return GroupedOpenApi.builder()
    //         .group("core-api")
    //         .displayName("Core APIs")
    //         .pathsToMatch("/api/**")
    //         .build();
    // }
    //
    // @Bean
    // GroupedOpenApi extensionApi() {
    //     return GroupedOpenApi.builder()
    //         .group("extension-api")
    //         .displayName("Extension APIs")
    //         .pathsToMatch("/apis/**")
    //         .pathsToExclude("/apis/api.console.halo.run/**", "/apis/api.halo.run/**",
    //             "/apis/api.plugin.halo.run/**")
    //         .build();
    // }
    //
    // @Bean
    // GroupedOpenApi systemCustomApi() {
    //     return GroupedOpenApi.builder()
    //         .group("core-custom-api")
    //         .displayName("Custom APIs in Core")
    //         .pathsToMatch("/apis/api.console.halo.run/**")
    //         .build();
    // }
    //
    // @Bean
    // GroupedOpenApi customApi() {
    //     return GroupedOpenApi.builder()
    //         .group("api.halo.run")
    //         .displayName("api.halo.run")
    //         .pathsToMatch("/apis/api.halo.run/**")
    //         .build();
    // }
    //
    // @Bean
    // GroupedOpenApi pluginCustomApi() {
    //     return GroupedOpenApi.builder()
    //         .group("plugin-custom-api")
    //         .displayName("Custom APIs in Plugin")
    //         .pathsToMatch("/apis/api.plugin.halo.run/**")
    //         .build();
    // }
    //
    // @Bean
    // GroupedOpenApi userCenterApi() {
    //     return GroupedOpenApi.builder()
    //         .group("uc.api")
    //         .displayName("User center APIs.")
    //         .pathsToMatch("/apis/uc.api.*/**")
    //         .build();
    // }
    //
    // @Bean
    // GroupedOpenApi allApi() {
    //     return GroupedOpenApi.builder()
    //         .group("all-api")
    //         .displayName("All APIs")
    //         .pathsToMatch("/api/**", "/apis/**", "/login/**")
    //         .build();
    // }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    ModelConverter customModelConverter(ObjectMapperProvider objectMapperProvider) {
        return new ModelResolver(objectMapperProvider.jsonMapper(), new CustomTypeNameResolver());
    }

    static class CustomTypeNameResolver extends TypeNameResolver {
        @Override
        protected String nameForClass(Class<?> cls, Set<Options> options) {
            // Obey the rule of keys that match the regular expression ^[a-zA-Z0-9\.\-_]+$.
            // See https://spec.openapis.org/oas/v3.0.3#fixed-fields-5 for more.
            return super.nameForClass(cls, options).replaceAll("\\$", ".");
        }
    }

}
