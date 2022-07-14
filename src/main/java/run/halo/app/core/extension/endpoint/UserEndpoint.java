package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.infra.utils.JsonUtils;

@Component
public class UserEndpoint implements CustomEndpoint {

    private final ExtensionClient client;
    private final UserService userService;

    public UserEndpoint(ExtensionClient client, UserService userService) {
        this.client = client;
        this.userService = userService;
    }

    @NonNull
    Mono<ServerResponse> me(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> {
                var name = ctx.getAuthentication().getName();
                return client.fetch(User.class, name)
                    .orElseThrow(() -> new ExtensionNotFoundException(name));
            })
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user));
    }

    @NonNull
    Mono<ServerResponse> grantPermission(ServerRequest request) {
        var username = request.pathVariable("name");
        return request.bodyToMono(GrantRequest.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body is empty")))
            .flatMap(grant -> {
                // preflight check
                client.fetch(User.class, username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User " + username + " was not found"));

                grant.roles.forEach(roleName -> client.fetch(Role.class, roleName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Role " + roleName + " was not found")));

                var bindings =
                    client.list(RoleBinding.class, RoleBinding.containsUser(username), null);
                var bindingToUpdate = new HashSet<RoleBinding>();
                var bindingToDelete = new HashSet<RoleBinding>();
                var existingRoles = new HashSet<String>();
                bindings.forEach(binding -> {
                    var roleName = binding.getRoleRef().getName();
                    if (grant.roles.contains(roleName)) {
                        existingRoles.add(roleName);
                        return;
                    }
                    binding.getSubjects().removeIf(RoleBinding.Subject.isUser(username));
                    if (CollectionUtils.isEmpty(binding.getSubjects())) {
                        // remove it if subjects is empty
                        bindingToDelete.add(binding);
                    } else {
                        bindingToUpdate.add(binding);
                    }
                });

                bindingToUpdate.forEach(client::update);
                bindingToDelete.forEach(client::delete);

                // remove existing roles
                var roles = new HashSet<>(grant.roles);
                roles.removeAll(existingRoles);
                roles.stream()
                    .map(roleName -> RoleBinding.create(username, roleName))
                    .forEach(client::create);

                return ServerResponse.ok().build();
            });
    }

    record GrantRequest(Set<String> roles) {
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.halo.run/v1alpha1/User";
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
                    .requestBody(
                        requestBodyBuilder().required(true).implementation(GrantRequest.class))
                    .response(responseBuilder().implementation(User.class)))
            .GET("/users/{name}/permissions", this::getUserPermission,
                builder -> builder.operationId("GetPermissions")
                    .description("Get permissions of user")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description("User name")
                        .required(true))
                    .response(responseBuilder().implementation(UserPermission.class)))
            .build();
    }

    @NonNull
    private Mono<ServerResponse> getUserPermission(ServerRequest request) {
        String name = request.pathVariable("name");
        return userService.listRoles(name)
            .reduce(new LinkedHashSet<Role>(), (list, role) -> {
                list.add(role);
                return list;
            })
            .map(roles -> {
                Set<String> uiPermissions =
                    roles.stream()
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

    record UserPermission(Set<Role> roles, Set<String> uiPermissions) {
    }
}
