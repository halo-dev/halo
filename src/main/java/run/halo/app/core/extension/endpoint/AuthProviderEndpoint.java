package run.halo.app.core.extension.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Auth provider endpoint.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class AuthProviderEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/AuthProvider";
        return SpringdocRouteBuilder.route()
            .GET("auth-providers", this::listAuthProviders,
                builder -> builder.operationId("listAuthProviders")
                    .description("Lists all auth providers")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementationArray(ListedAuthProvider.class))
            )
            .PUT("auth-providers/{name}/enable", this::enableAuthProvider,
                builder -> builder.operationId("enableAuthProvider")
                    .description("Enables an auth provider")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(AuthProvider.class))
            )
            .PUT("auth-providers/{name}/disable", this::disableAuthProvider,
                builder -> builder.operationId("disableAuthProvider")
                    .description("Disables an auth provider")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(AuthProvider.class))
            )
            .GET("auth-providers/-/enabled", this::listEnabledAuthProviders,
                builder -> builder.operationId("listEnabledAuthProviders")
                    .description("Lists all enabled auth providers")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementationArray(ListedAuthProvider.class))
            )
            .build();
    }

    private Mono<ServerResponse> listEnabledAuthProviders(ServerRequest request) {
        return fetchEnabledAuthProviders()
            .flatMapMany(Flux::fromIterable)
            .flatMap(name -> client.fetch(AuthProvider.class, name))
            .map(this::convertTo)
            .collectList()
            .flatMap(listed -> ServerResponse.ok().bodyValue(listed));
    }

    private Mono<ServerResponse> disableAuthProvider(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.fetch(AuthProvider.class, name)
            .flatMap(authProvider -> updateAuthProviderEnabled(enabled -> enabled.remove(name))
                .thenReturn(authProvider)
            )
            .flatMap(authProvider -> ServerResponse.ok().bodyValue(authProvider));
    }

    private Mono<ServerResponse> enableAuthProvider(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.fetch(AuthProvider.class, name)
            .flatMap(authProvider -> updateAuthProviderEnabled(enabled -> enabled.add(name))
                .thenReturn(authProvider)
            )
            .flatMap(authProvider -> ServerResponse.ok().bodyValue(authProvider));
    }

    private Mono<ConfigMap> updateAuthProviderEnabled(Consumer<Set<String>> consumer) {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
            .flatMap(configMap -> {
                SystemSetting.AuthProvider authProvider = getAuthProvider(configMap);
                final Map<String, String> data = configMap.getData();
                consumer.accept(authProvider.getEnabled());
                data.put(SystemSetting.AuthProvider.GROUP, JsonUtils.objectToJson(authProvider));
                return client.update(configMap);
            });
    }

    private Mono<Set<String>> fetchEnabledAuthProviders() {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
            .map(configMap -> {
                SystemSetting.AuthProvider authProvider = getAuthProvider(configMap);
                return authProvider.getEnabled();
            });
    }

    @NonNull
    private static SystemSetting.AuthProvider getAuthProvider(ConfigMap configMap) {
        if (configMap.getData() == null) {
            configMap.setData(new HashMap<>());
        }
        final Map<String, String> data = configMap.getData();
        String providerGroup = data.get(SystemSetting.AuthProvider.GROUP);

        SystemSetting.AuthProvider authProvider;
        if (StringUtils.isBlank(providerGroup)) {
            authProvider = new SystemSetting.AuthProvider();
        } else {
            authProvider =
                JsonUtils.jsonToObject(providerGroup, SystemSetting.AuthProvider.class);
        }

        if (authProvider.getEnabled() == null) {
            authProvider.setEnabled(new HashSet<>());
        }
        return authProvider;
    }

    Mono<ServerResponse> listAuthProviders(ServerRequest request) {
        return client.list(AuthProvider.class, provider ->
                    provider.getMetadata().getDeletionTimestamp() == null,
                Comparator.comparing(item -> item.getMetadata().getCreationTimestamp())
            )
            .filter(authProvider -> StringUtils.isNotBlank(authProvider.getSpec().getBindingUrl()))
            .map(this::convertTo)
            .collectList()
            .flatMap(providers -> listMyConnections()
                .map(connection -> connection.getSpec().getRegistrationId())
                .collectList()
                .map(connectedNames -> providers.stream()
                    .map(provider -> {
                        boolean isBound = connectedNames.contains(provider.getName());
                        return provider.withIsBound(isBound);
                    })
                    .collect(Collectors.toList())
                )
                .defaultIfEmpty(providers)
            )
            .flatMap(providers -> ServerResponse.ok().bodyValue(providers));
    }

    private ListedAuthProvider convertTo(AuthProvider authProvider) {
        return ListedAuthProvider.builder()
            .name(authProvider.getMetadata().getName())
            .displayName(authProvider.getSpec().getDisplayName())
            .logo(authProvider.getSpec().getLogo())
            .website(authProvider.getSpec().getWebsite())
            .authenticationUrl(authProvider.getSpec().getAuthenticationUrl())
            .helpPage(authProvider.getSpec().getHelpPage())
            .bindingUrl(authProvider.getSpec().getBindingUrl())
            .unbindingUrl(authProvider.getSpec().getUnbindUrl())
            .isBound(false)
            .build();
    }

    Flux<UserConnection> listMyConnections() {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication().getName())
            .switchIfEmpty(Mono.error(
                new AccessDeniedException("Cannot list connections without user authentication")))
            .flatMapMany(username -> client.list(UserConnection.class,
                    persisted -> persisted.getSpec().getUsername().equals(username),
                    Comparator.comparing(item -> item.getMetadata()
                        .getCreationTimestamp())
                )
            );
    }

    @Value
    @Builder
    public static class ListedAuthProvider {
        @Schema(requiredMode = REQUIRED)
        String name;

        @Schema(requiredMode = REQUIRED)
        String displayName;

        String logo;

        String website;

        String authenticationUrl;

        String helpPage;

        String bindingUrl;

        String unbindingUrl;

        @With
        Boolean isBound;
    }
}
