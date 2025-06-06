package run.halo.app.core.endpoint.uc;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.HashMap;
import java.util.Objects;
import org.springdoc.core.fn.builders.requestbody.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * User preference endpoint for UC (User Center).
 * This endpoint allows users to get and update their preferences by group.
 *
 * @author JohnNiang
 * @since 2.21.0
 */
@Component
class UcUserPreferenceEndpoint implements CustomEndpoint {

    private static final String PREFERENCE_PREFIX = "user-preferences-";

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    private final ReactiveExtensionClient client;

    private final ObjectMapper mapper;

    UcUserPreferenceEndpoint(ReactiveExtensionClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "UserPreferenceV1alpha1Uc";
        return SpringdocRouteBuilder.route()
            .GET(
                "/user-preferences/{group}",
                this::getMyPreference,
                builder -> builder.operationId("getMyPreference")
                    .tag(tag)
                    .description("Get my preference by group.")
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("group")
                        .description("Group of user preference, e.g. `notification`.")
                        .implementation(String.class)
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(JsonNode.class)
                    )
            )
            .PUT(
                "/user-preferences/{group}",
                this::updateMyPreference,
                builder -> builder.operationId("updateMyPreference")
                    .tag(tag)
                    .description("Create or update my preference by group.")
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("group")
                        .description("Group of user preference, e.g. `notification`.")
                        .implementation(String.class)
                        .required(true)
                    )
                    .requestBody(Builder.requestBodyBuilder()
                        .required(true)
                        .implementation(JsonNode.class))
                    .response(responseBuilder()
                        .description("No content, preference updated successfully.")
                        .responseCode(String.valueOf(HttpStatus.NO_CONTENT.value()))
                    )
            )
            .build();
    }

    private Mono<ServerResponse> updateMyPreference(ServerRequest serverRequest) {
        var group = serverRequest.pathVariable("group");
        return authenticated()
            .map(Authentication::getName)
            .flatMap(username -> client.fetch(ConfigMap.class, PREFERENCE_PREFIX + username)
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    var cm = new ConfigMap();
                    cm.setMetadata(new Metadata());
                    cm.getMetadata().setName(PREFERENCE_PREFIX + username);
                    return cm;
                }))
            )
            .flatMap(cm -> serverRequest.bodyToMono(JsonNode.class)
                .switchIfEmpty(
                    Mono.error(() -> new ServerWebInputException("Request body is required."))
                )
                .flatMap(jsonNode -> Mono.fromCallable(() -> {
                    if (cm.getData() == null) {
                        cm.setData(new HashMap<>());
                    }
                    var json = mapper.writeValueAsString(jsonNode);
                    if (Objects.equals(json, cm.getData().get(group))) {
                        return null;
                    }
                    cm.getData().put(group, json);
                    return cm;
                }))
                .flatMap(extension -> {
                    if (extension.getMetadata().getVersion() == null) {
                        return client.create(extension);
                    }
                    return client.update(extension);
                })
                .defaultIfEmpty(cm)
            )
            .flatMap(cm -> ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> getMyPreference(ServerRequest serverRequest) {
        var group = serverRequest.pathVariable("group");
        return authenticated()
            .map(Authentication::getName)
            .flatMap(username -> client.fetch(ConfigMap.class, PREFERENCE_PREFIX + username))
            .mapNotNull(ConfigMap::getData)
            .mapNotNull(data -> data.get(group))
            .flatMap(json -> Mono.fromCallable(() -> mapper.readTree(json)))
            .defaultIfEmpty(NullNode.getInstance())
            .flatMap(jsonNode -> ServerResponse.ok().bodyValue(jsonNode));
    }

    private Mono<Authentication> authenticated() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(trustResolver::isAuthenticated)
            .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Anonymous user is not allowed to access user preference."
            )));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.halo.run/v1alpha1");
    }
}
