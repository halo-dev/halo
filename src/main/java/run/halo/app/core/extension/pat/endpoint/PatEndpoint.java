package run.halo.app.core.extension.pat.endpoint;

import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.HashMap;
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
import run.halo.app.core.extension.pat.Constant;
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
                        .description("The raw token is put into the annotations which name is "
                            + Constant.RAW_TOKEN_ANNO_KEY)
                        .implementation(PersonalAccessToken.class));
            })
            .build();
    }

    @NonNull
    private Mono<ServerResponse> newToken(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(authentication -> this.buildAndCreatePat(request, authentication))
            .flatMap(pat -> ServerResponse.ok().bodyValue(pat));
    }

    private Mono<PersonalAccessToken> buildAndCreatePat(ServerRequest request,
                                                        Authentication authentication) {
        return request.bodyToMono(NewTokenRequest.class)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Request body is required")))
            .map(tokenRequest -> this.buildPat(tokenRequest, authentication))
            .flatMap(this::buildTokenAndCreatePat);
    }

    private Mono<PersonalAccessToken> createPat(String rawToken,
                                                PersonalAccessToken pat) {
        return encoder.encode(rawToken)
            .doOnNext(encodedToken -> {
                pat.getSpec().setEncodedToken(encodedToken);
            })
            .map(encodedToken -> pat)
            .flatMap(client::create)
            .doOnNext(created -> {
                var annotations = created.getMetadata().getAnnotations();
                if (annotations == null) {
                    annotations = new HashMap<>();
                    created.getMetadata().setAnnotations(annotations);
                }
                annotations.put(Constant.RAW_TOKEN_ANNO_KEY, rawToken);
            });
    }

    private PersonalAccessToken buildPat(NewTokenRequest tokenRequest,
                                         Authentication authentication) {
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
    }

    private Mono<PersonalAccessToken> buildTokenAndCreatePat(PersonalAccessToken pat) {
        return this.encoder.buildToken(pat)
            .flatMap(rawToken -> this.createPat(rawToken, pat));
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

}
