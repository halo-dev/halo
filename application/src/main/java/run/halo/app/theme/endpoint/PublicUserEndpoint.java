package run.halo.app.theme.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.GroupVersion;

/**
 * User endpoint for unauthenticated user.
 *
 * @author guqing
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class PublicUserEndpoint implements CustomEndpoint {
    private final UserService userService;
    private final ServerSecurityContextRepository securityContextRepository;
    private final ReactiveUserDetailsService reactiveUserDetailsService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.halo.run/v1alpha1/User";
        return SpringdocRouteBuilder.route()
            .POST("/users/-/signup", this::signUp,
                builder -> builder.operationId("SignUp")
                    .description("Sign up a new user")
                    .tag(tag)
                    .requestBody(requestBodyBuilder().required(true)
                        .implementation(SignUpRequest.class)
                    )
                    .response(responseBuilder().implementation(User.class))
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> signUp(ServerRequest request) {
        return request.bodyToMono(SignUpRequest.class)
            .flatMap(signUpRequest ->
                userService.signUp(signUpRequest.user(), signUpRequest.password())
            )
            .flatMap(user -> authenticate(user.getMetadata().getName(), request.exchange())
                .thenReturn(user)
            )
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
            );
    }

    private Mono<Void> authenticate(String username, ServerWebExchange exchange) {
        return reactiveUserDetailsService.findByUsername(username)
            .flatMap(userDetails -> {
                SecurityContextImpl securityContext = new SecurityContextImpl();
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                        userDetails.getPassword(), userDetails.getAuthorities());
                securityContext.setAuthentication(authentication);
                return securityContextRepository.save(exchange, securityContext);
            });
    }

    record SignUpRequest(@Schema(requiredMode = REQUIRED) User user,
                         @Schema(requiredMode = REQUIRED, minLength = 6) String password) {
    }
}
