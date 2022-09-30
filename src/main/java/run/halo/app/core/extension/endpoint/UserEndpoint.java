package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

@Component
public class UserEndpoint implements CustomEndpoint {

    private static final String SELF_USER = "-";
    private final ReactiveExtensionClient client;
    private final UserService userService;

    public UserEndpoint(ReactiveExtensionClient client, UserService userService) {
        this.client = client;
        this.userService = userService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/User";
        return SpringdocRouteBuilder.route()
            .GET("/users/-", this::me, builder -> builder.operationId("GetCurrentUserDetail")
                .description("Get current user detail")
                .tag(tag)
                .response(responseBuilder().implementation(User.class)))
            .POST("/users/{name}/permissions", this::grantPermission,
                builder -> builder.operationId("GrantPermission")
                    .description("Grant permissions to user")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description("User name")
                        .required(true))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(GrantRequest.class))
                    .response(responseBuilder().implementation(User.class)))
            .GET("/users/{name}/permissions", this::getUserPermission,
                builder -> builder.operationId("GetPermissions")
                    .description("Get permissions of user")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description("User name")
                        .required(true))
                    .response(responseBuilder().implementation(UserPermission.class)))
            .PUT("/users/{name}/password", this::changePassword,
                builder -> builder.operationId("ChangePassword")
                    .description("Change password of user.")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description(
                            "Name of user. If the name is equal to '-', it will change the "
                                + "password of current user.")
                        .required(true))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(ChangePasswordRequest.class))
                    .response(responseBuilder()
                        .implementation(User.class))
            )
            .build();
    }

    Mono<ServerResponse> changePassword(ServerRequest request) {
        final var nameInPath = request.pathVariable("name");
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> SELF_USER.equals(nameInPath) ? ctx.getAuthentication().getName()
                : nameInPath)
            .flatMap(username -> request.bodyToMono(ChangePasswordRequest.class)
                .switchIfEmpty(Mono.defer(() ->
                    Mono.error(new ServerWebInputException("Request body is empty"))))
                .flatMap(changePasswordRequest -> {
                    var password = changePasswordRequest.password();
                    // encode password
                    return userService.updateWithRawPassword(username, password);
                }))
            .flatMap(updatedUser -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser));
    }

    record ChangePasswordRequest(
        @Schema(description = "New password.", required = true, minLength = 6)
        String password) {
    }

    @NonNull
    Mono<ServerResponse> me(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> {
                var name = ctx.getAuthentication().getName();
                return client.get(User.class, name);
            })
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user));
    }

    @NonNull
    Mono<ServerResponse> grantPermission(ServerRequest request) {
        var username = request.pathVariable("name");
        return request.bodyToMono(GrantRequest.class)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Request body is empty")))
            .flatMap(grantRequest -> userService.grantRoles(username, grantRequest.roles())
                .then(ServerResponse.ok().build()));
    }

    private Mono<GrantRequest> checkRoles(GrantRequest request) {
        return Flux.fromIterable(request.roles)
            .flatMap(role -> client.get(Role.class, role))
            .then(Mono.just(request));
    }

    record GrantRequest(Set<String> roles) {
    }

    @NonNull
    private Mono<ServerResponse> getUserPermission(ServerRequest request) {
        String name = request.pathVariable("name");
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> SELF_USER.equals(name) ? ctx.getAuthentication().getName() : name)
            .flatMapMany(userService::listRoles)
            .reduce(new LinkedHashSet<Role>(), (list, role) -> {
                list.add(role);
                return list;
            })
            .map(roles -> {
                Set<String> uiPermissions = roles.stream()
                    .map(role -> role.getMetadata().getAnnotations())
                    .filter(Objects::nonNull)
                    .map(this::mergeUiPermissions)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
                return new UserPermission(roles, uiPermissions);
            })
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    private Set<String> mergeUiPermissions(Map<String, String> annotations) {
        Set<String> result = new LinkedHashSet<>();
        String permissionsStr = annotations.get(Role.UI_PERMISSIONS_AGGREGATED_ANNO);
        if (StringUtils.isNotBlank(permissionsStr)) {
            result.addAll(JsonUtils.jsonToObject(permissionsStr,
                new TypeReference<LinkedHashSet<String>>() {
                }));
        }
        String uiPermissionStr = annotations.get(Role.UI_PERMISSIONS_ANNO);
        if (StringUtils.isNotBlank(uiPermissionStr)) {
            result.addAll(JsonUtils.jsonToObject(uiPermissionStr,
                new TypeReference<LinkedHashSet<String>>() {
                }));
        }
        return result;
    }

    record UserPermission(@Schema(required = true) Set<Role> roles,
                          @Schema(required = true) Set<String> uiPermissions) {
    }
}
