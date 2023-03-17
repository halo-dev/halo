package run.halo.app.core.extension.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.AccessDeniedException;

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
            .build();
    }

    Mono<ServerResponse> listAuthProviders(ServerRequest request) {
        return client.list(AuthProvider.class, provider ->
                    provider.getMetadata().getDeletionTimestamp() == null,
                Comparator.comparing(item -> item.getMetadata().getCreationTimestamp())
            )
            .map(authProvider -> ListedAuthProvider.builder()
                .name(authProvider.getMetadata().getName())
                .displayName(authProvider.getSpec().getDisplayName())
                .logo(authProvider.getSpec().getLogo())
                .website(authProvider.getSpec().getWebsite())
                .authenticationUrl(authProvider.getSpec().getAuthenticationUrl())
                .helpPage(authProvider.getSpec().getHelpPage())
                .bindingUrl(authProvider.getSpec().getBindingUrl())
                .unbindingUrl(authProvider.getSpec().getUnbindUrl())
                .isBound(false)
                .build()
            )
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
