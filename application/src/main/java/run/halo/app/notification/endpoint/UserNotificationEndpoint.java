package run.halo.app.notification.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
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
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
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
                })
            .build();
    }

    Supplier<RouterFunction<ServerResponse>> userspaceScopedApis() {
        var tag = "api.notification.halo.run/v1alpha1/Notification";
        return () -> SpringdocRouteBuilder.route()
            .GET("/notifications", this::listNotification,
                builder -> {
                    builder.operationId("ListUserNotifications")
                        .description("List notifications for the authenticated user.")
                        .tag(tag)
                        .parameter(parameterBuilder()
                            .in(ParameterIn.PATH)
                            .name("username")
                            .description("Username")
                            .required(true)
                        )
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(Notification.class))
                        );
                    UserNotificationQuery.buildParameters(builder);
                }
            )
            .PUT("/notifications/{name}/mark-as-read", this::markNotificationAsRead,
                builder -> builder.operationId("MarkNotificationAsRead")
                    .description("Mark the specified notification as read.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("username")
                        .description("Username")
                        .required(true)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Notification name")
                        .required(true)
                    )
                    .response(responseBuilder().implementation(Notification.class))
            )
            .PUT("/notifications/-/mark-specified-as-read", this::markNotificationsAsRead,
                builder -> builder.operationId("MarkNotificationsAsRead")
                    .description("Mark the specified notifications as read.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("username")
                        .description("Username")
                        .required(true)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(MarkSpecifiedRequest.class))
                        )
                    )
                    .response(responseBuilder().implementationArray(String.class))
            )
            .DELETE("/notifications/{name}", this::deleteNotification,
                builder -> builder.operationId("DeleteSpecifiedNotification")
                    .description("Delete the specified notification.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("username")
                        .description("Username")
                        .required(true)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Notification name")
                        .required(true)
                    )
                    .response(responseBuilder().implementation(Notification.class))
            )
            .build();
    }

    private Mono<ServerResponse> deleteNotification(ServerRequest request) {
        var name = request.pathVariable("name");
        var username = request.pathVariable("username");
        return notificationService.deleteByName(username, name)
            .flatMap(notification -> ServerResponse.ok().bodyValue(notification));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.notification.halo.run/v1alpha1");
    }

    record MarkSpecifiedRequest(List<String> names) {
    }

    private Mono<ServerResponse> listNotification(ServerRequest request) {
        var username = request.pathVariable("username");
        var query = new UserNotificationQuery(request.exchange(), username);
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
        return request.bodyToMono(MarkSpecifiedRequest.class)
            .flatMapMany(
                requestBody -> notificationService.markSpecifiedAsRead(username, requestBody.names))
            .collectList()
            .flatMap(names -> ServerResponse.ok().bodyValue(names));
    }
}
