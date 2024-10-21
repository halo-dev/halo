package run.halo.app.infra.config;

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

    @Bean
    GroupedOpenApi aggregatedV1alpha1Api() {
        return GroupedOpenApi.builder()
            .group("apis_aggregated.api_v1alpha1")
            .displayName("Aggregated API V1alpha1")
            .pathsToMatch(
                "/apis/*/v1alpha1/**",
                "/api/v1alpha1/**",
                "/login/**",
                "/system/setup"
            )
            .build();
    }

    @Bean
    GroupedOpenApi publicV1alpha1Api() {
        return GroupedOpenApi.builder()
            .group("apis_public.api_v1alpha1")
            .displayName("Public API V1alpha1")
            .pathsToMatch(
                "/apis/api.*/**"
            )
            .pathsToExclude(
                "/apis/api.console.*/v1alpha1/**",
                // compatible with legacy issues
                "/apis/api.notification.halo.run/v1alpha1/userspaces/**",
                "/apis/api.notification.halo.run/v1alpha1/notifiers/**"
            )
            .build();
    }

    @Bean
    GroupedOpenApi consoleV1alpha1Api() {
        return GroupedOpenApi.builder()
            .group("apis_console.api_v1alpha1")
            .displayName("Console API V1alpha1")
            .pathsToMatch(
                "/apis/console.api.*/v1alpha1/**",
                "/apis/api.console.halo.run/v1alpha1/**"
            )
            .build();
    }


    @Bean
    GroupedOpenApi ucV1alpha1Api() {
        return GroupedOpenApi.builder()
            .group("apis_uc.api_v1alpha1")
            .displayName("User-center API V1alpha1")
            .pathsToMatch(
                "/apis/uc.api.*/v1alpha1/**",
                // compatible with legacy issues
                "/apis/api.notification.halo.run/v1alpha1/userspaces/**",
                "/apis/api.notification.halo.run/v1alpha1/notifiers/**"
            )
            .build();
    }


    @Bean
    GroupedOpenApi extensionV1alpha1Api() {
        return GroupedOpenApi.builder()
            .group("apis_extension.api_v1alpha1")
            .displayName("Extension API V1alpha1")
            .pathsToMatch(
                "/api/v1alpha1/**",
                "/apis/content.halo.run/v1alpha1/**",
                "/apis/theme.halo.run/v1alpha1/**",
                "/apis/security.halo.run/v1alpha1/**",
                "/apis/migration.halo.run/v1alpha1/**",
                "/apis/auth.halo.run/v1alpha1/**",
                "/apis/metrics.halo.run/v1alpha1/**",
                "/apis/storage.halo.run/v1alpha1/**",
                "/apis/plugin.halo.run/v1alpha1/**",
                "/apis/notification.halo.run/**",
                "/apis/migration.halo.run/**"
            )
            .build();
    }

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
