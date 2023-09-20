package run.halo.app.notification.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.notification.UserNotificationQuery;
import run.halo.app.notification.UserNotificationService;

/**
 * Custom notification endpoint to managing notification for authenticated user.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class UserNotificationEndpoint implements CustomEndpoint {

    private final UserNotificationService notificationService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return SpringdocRouteBuilder.route()
            .nest(RequestPredicates.path("/userspaces/{username}"), userspaceScopedApis(),
                builder -> {
                    builder.parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("username")
                        .description("Username")
                        .required(true)
                    );
                })
            .build();
    }

    Supplier<RouterFunction<ServerResponse>> userspaceScopedApis() {
        var tag = "api.console.halo.run/v1alpha1/Notification";
        return () -> SpringdocRouteBuilder.route()
            .GET("/notifications", this::listNotification,
                builder -> {
                    builder.operationId("ListUserNotifications")
                        .description("List notifications for the authenticated user.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(Notification.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder,
                        UserNotificationQuery.class);
                }
            )
            .PUT("/notifications/{name}/mark-as-read", this::markNotificationAsRead,
                builder -> builder.operationId("MarkNotificationAsRead")
                    .description("Mark the specified notification as read.")
                    .tag(tag)
                    .response(responseBuilder().implementation(Notification.class))
            )
            .PUT("/notifications/-/mark-specified-as-read", this::markNotificationsAsRead,
                builder -> builder.operationId("MarkNotificationsAsRead")
                    .description("Mark the specified notifications as read.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(String[].class)
                            )
                        )
                    )
                    .response(responseBuilder().implementationArray(String.class))
            )
            .build();
    }

    private Mono<ServerResponse> listNotification(ServerRequest request) {
        var query = new UserNotificationQuery(request.exchange());
        var username = request.pathVariable("username");
        return notificationService.listByUser(username, query)
            .flatMap(notifications -> ServerResponse.ok().bodyValue(notifications));
    }

    private Mono<ServerResponse> markNotificationAsRead(ServerRequest request) {
        var username = request.pathVariable("username");
        var name = request.pathVariable("name");
        return notificationService.markAsRead(username, name)
            .flatMap(notification -> ServerResponse.ok().bodyValue(notification));
    }

    Mono<ServerResponse> markNotificationsAsRead(ServerRequest request) {
        var username = request.pathVariable("username");
        return request.bodyToFlux(String.class)
            .collectList()
            .flatMapMany(names -> notificationService.markSpecifiedAsRead(username, names))
            .collectList()
            .flatMap(names -> ServerResponse.ok().bodyValue(names));
    }
}
