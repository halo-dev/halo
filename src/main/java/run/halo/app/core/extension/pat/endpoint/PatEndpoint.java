package run.halo.app.core.extension.pat.endpoint;

import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.pat.PersonalAccessToken;
import run.halo.app.core.extension.pat.PersonalAccessToken.PersonalAccessTokenSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authentication.pat.PatEncoder;

@Component
public class PatEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final PatEncoder encoder;

    public PatEndpoint(ReactiveExtensionClient client, PatEncoder encoder) {
        this.client = client;
        this.encoder = encoder;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/PersonalAccessToken";
        return SpringdocRouteBuilder.route()
            .POST("/personalaccesstokens", this::newToken, builder -> {
                builder.operationId("CreatePersonalAccessToken")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(TokenRequest.class));
            })
            .build();
    }

    @NonNull
    private Mono<ServerResponse> newToken(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> request.bodyToMono(TokenRequest.class)
                .switchIfEmpty(
                    Mono.error(() -> new ServerWebInputException("Request body is required")))
                .flatMap(tokenRequest -> {
                    // create a Personal Access Token
                    var metadata = new Metadata();
                    var username = authentication.getName();
                    metadata.setName(UUID.randomUUID().toString());
                    var spec = new PersonalAccessTokenSpec();
                    spec.setCreatedBy(username);
                    spec.setDescription(tokenRequest.getDescription());
                    spec.setScopes(tokenRequest.getScopes());
                    spec.setExpiresAt(tokenRequest.getExpiresAt());
                    spec.setRevoked(false);
                    var pat = new PersonalAccessToken();
                    pat.setMetadata(metadata);
                    pat.setSpec(spec);
                    return encoder.buildToken(pat)
                        .flatMap(rawToken -> encoder.encode(rawToken)
                            .flatMap(encodedToken -> {
                                spec.setEncodedToken(encodedToken);
                                return client.create(pat);
                            })
                            .thenReturn(rawToken));
                })
                .flatMap(rawToken -> ServerResponse.ok()
                    .bodyValue(Map.of("rawToken", rawToken))));
    }

    @Data
    public static class TokenRequest {

        private String description;

        private Instant expiresAt;

        private Set<String> scopes;
    }
}
