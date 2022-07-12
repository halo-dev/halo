package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.HashSet;
import java.util.Set;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@Component
public class UserEndpoint implements CustomEndpoint {

    private final ExtensionClient client;

    public UserEndpoint(ExtensionClient client) {
        this.client = client;
    }

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
            .build();
    }

}
