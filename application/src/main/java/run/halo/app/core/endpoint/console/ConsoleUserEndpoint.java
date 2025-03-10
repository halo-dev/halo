package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.Objects;
import org.springdoc.core.fn.builders.parameter.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.GroupVersion;

/**
 * User endpoint for console.
 *
 * @author johnniang
 */
@Component
class ConsoleUserEndpoint implements CustomEndpoint {

    private final UserService userService;

    ConsoleUserEndpoint(UserService userService) {
        this.userService = userService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "UserV1alpha1Console";
        return RouterFunctions.nest(RequestPredicates.path("/users"), SpringdocRouteBuilder.route()
            .POST("/{username}/disable", this::handleDisableUser, ops -> {
                ops.operationId("DisableUser")
                    .tag(tag)
                    .description("Disable user by username")
                    .parameter(Builder.parameterBuilder()
                        .name("username")
                        .in(ParameterIn.PATH)
                        .description("Username")
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(User.class)
                        .description("The user has been disabled.")
                    );
            })
            .POST("/{username}/enable", this::handleEnableUser, ops -> {
                ops.operationId("EnableUser")
                    .tag(tag)
                    .description("Enable user by username")
                    .parameter(Builder.parameterBuilder()
                        .name("username")
                        .in(ParameterIn.PATH)
                        .description("Username")
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(User.class)
                        .description("The user has been enabled.")
                    );
            })
            .build());
    }

    private Mono<ServerResponse> handleEnableUser(ServerRequest request) {
        return userService.enable(request.pathVariable("username"))
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("The user was not found or has been enabled."))
            )
            .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    private Mono<ServerResponse> handleDisableUser(ServerRequest request) {
        var username = request.pathVariable("username");
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("The current user is not authenticated."))
            )
            .filter(currentUsername -> !Objects.equals(currentUsername, username))
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The user is the current user, can't disable it."
            )))
            .then(Mono.defer(() -> userService.disable(username)))
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("The user was not found or has been disabled."))
            )
            .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("console.api.security.halo.run", "v1alpha1");
    }
}
