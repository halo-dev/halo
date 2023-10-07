package run.halo.app.notification.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.notification.UserNotificationPreference;
import run.halo.app.notification.UserNotificationPreferenceService;

/**
 * Endpoint for user notification preferences.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class UserNotificationPreferencesEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;
    private final UserNotificationPreferenceService userNotificationPreferenceService;

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
            .GET("/notification-preferences", this::listNotificationPreferences,
                builder -> builder.operationId("ListUserNotificationPreferences")
                    .description("List notification preferences for the authenticated user.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("username")
                        .description("Username")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementationArray(ReasonTypeNotifierMatrix.class)
                    )
            )
            .POST("/notification-preferences", this::saveNotificationPreferences,
                builder -> builder.operationId("SaveUserNotificationPreferences")
                    .description("Save notification preferences for the authenticated user.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("username")
                        .description("Username")
                        .required(true)
                    )
                    .requestBody(requestBodyBuilder()
                        .implementation(ReasonTypeNotifierCollectionRequest.class)
                    )
                    .response(responseBuilder().implementation(ReasonTypeNotifierMatrix.class))
            )
            .build();
    }

    private Mono<ServerResponse> saveNotificationPreferences(ServerRequest request) {
        var username = request.pathVariable("username");
        return request.bodyToMono(ReasonTypeNotifierCollectionRequest.class)
            .flatMap(requestBody -> {
                var reasonTypNotifiers = requestBody.reasonTypeNotifiers();
                return userNotificationPreferenceService.getByUser(username)
                    .flatMap(preference -> {
                        var reasonTypeNotifierMap = preference.getReasonTypeNotifier();
                        reasonTypeNotifierMap.clear();
                        reasonTypNotifiers.forEach(reasonTypeNotifierRequest -> {
                            var reasonType = reasonTypeNotifierRequest.getReasonType();
                            var notifiers = reasonTypeNotifierRequest.getNotifiers();
                            var notifierSetting = new UserNotificationPreference.NotifierSetting();
                            notifierSetting.setNotifiers(
                                notifiers == null ? Set.of() : Set.copyOf(notifiers));
                            reasonTypeNotifierMap.put(reasonType, notifierSetting);
                        });
                        return userNotificationPreferenceService.saveByUser(username, preference);
                    });
            })
            .then(Mono.defer(() -> listReasonTypeNotifierMatrix(username)
                .collectList()
                .flatMap(result -> ServerResponse.ok().bodyValue(result)))
            );
    }

    private Mono<ServerResponse> listNotificationPreferences(ServerRequest request) {
        var username = request.pathVariable("username");
        return listReasonTypeNotifierMatrix(username)
            .collectList()
            .flatMap(matrix -> ServerResponse.ok().bodyValue(matrix));
    }

    Flux<ReasonTypeNotifierMatrix> listReasonTypeNotifierMatrix(String username) {
        return client.list(ReasonType.class, null, Comparators.defaultComparator())
            .map(reasonType -> new ReasonTypeNotifierMatrix()
                .setName(reasonType.getMetadata().getName())
                .setDisplayName(reasonType.getSpec().getDisplayName())
                .setDescription(reasonType.getSpec().getDescription())
            )
            .flatMap(notifierMatrix -> userNotificationPreferenceService.getByUser(username)
                .doOnNext(preference -> {
                    var notifiers = preference.getReasonTypeNotifier()
                        .getNotifiers(notifierMatrix.getName());
                    notifierMatrix.setNotifiers(List.copyOf(notifiers));
                })
                .thenReturn(notifierMatrix)
            );
    }

    @Data
    @Accessors(chain = true)
    static class ReasonTypeNotifierMatrix {
        private String name;
        private String displayName;
        private String description;
        private List<String> notifiers;
    }

    record ReasonTypeNotifierCollectionRequest(
        @Schema(requiredMode = REQUIRED) List<ReasonTypeNotifierRequest> reasonTypeNotifiers) {
    }

    @Data
    static class ReasonTypeNotifierRequest {
        private String reasonType;
        private List<String> notifiers;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.notification.halo.run/v1alpha1");
    }
}
