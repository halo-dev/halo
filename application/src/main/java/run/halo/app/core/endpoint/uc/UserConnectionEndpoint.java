package run.halo.app.core.endpoint.uc;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.parameter.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.extension.GroupVersion;

/**
 * User connection endpoint.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
public class UserConnectionEndpoint implements CustomEndpoint {

    private final UserConnectionService connectionService;

    private final AuthenticationTrustResolver authenticationTrustResolver =
        new AuthenticationTrustResolverImpl();

    public UserConnectionEndpoint(UserConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "UserConnectionV1alpha1Uc";
        return SpringdocRouteBuilder.route()
            .PUT(
                "/user-connections/{registerId}/disconnect",
                request -> {
                    var removedUserConnections = ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(authenticationTrustResolver::isAuthenticated)
                        .map(Authentication::getName)
                        .flatMapMany(username -> connectionService.removeUserConnection(
                            request.pathVariable("registerId"), username)
                        );
                    return ServerResponse.ok().body(removedUserConnections, UserConnection.class);
                },
                builder -> builder.operationId("DisconnectMyConnection")
                    .description("Disconnect my connection from a third-party platform.")
                    .tag(tag)
                    .parameter(Builder.parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("registerId")
                        .description("The registration ID of the third-party platform.")
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder().implementationArray(UserConnection.class))
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.auth.halo.run/v1alpha1");
    }
}
