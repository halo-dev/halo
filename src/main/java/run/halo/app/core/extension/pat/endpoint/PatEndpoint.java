package run.halo.app.core.extension.pat.endpoint;

import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
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
                        .implementation(NewTokenRequest.class))
                    .response(Builder.responseBuilder()
                        .implementation(NewTokenResponse.class));
            })
            .build();
    }

    @NonNull
    private Mono<ServerResponse> newToken(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> this.createPat(request, authentication));
    }

    private Mono<ServerResponse> createPat(ServerRequest request, Authentication authentication) {
        return request.bodyToMono(NewTokenRequest.class)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Request body is required")))
            .flatMap(tokenRequest -> this.createPat(tokenRequest, authentication))
            .flatMap(tokenResponse -> ServerResponse.ok().bodyValue(tokenResponse));
    }

    private Mono<NewTokenResponse> createPat(NewTokenRequest tokenRequest,
                                             Authentication authentication) {

        return Mono.fromCallable(
                () -> {
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
                    return pat;
                })
            .flatMap(this::buildToken)
            .map(NewTokenResponse::new);
    }

    private Mono<PersonalAccessToken> createPat(String rawToken,
                                                PersonalAccessToken pat) {
        return encoder.encode(rawToken)
            .doOnNext(encodedToken -> {
                pat.getSpec().setEncodedToken(encodedToken);
            })
            .map(encodedToken -> pat)
            .flatMap(client::create);
    }

    private Mono<String> buildToken(PersonalAccessToken pat) {
        return encoder.buildToken(pat)
            .flatMap(rawToken -> this.createPat(rawToken, pat).thenReturn(rawToken));
    }

    @Data
    public static class NewTokenRequest {

        @Schema(required = true, description = "Token description")
        private String description;

        @Schema(nullable = true, description = "Expiration time")
        private Instant expiresAt;

        @Schema(nullable = true, description = "Scopes the token could be accessed")
        private Set<String> scopes;
    }

    public record NewTokenResponse(
        @Schema(required = true, description = "Raw token that could be used for authentication")
        String rawToken) {

    }
}
