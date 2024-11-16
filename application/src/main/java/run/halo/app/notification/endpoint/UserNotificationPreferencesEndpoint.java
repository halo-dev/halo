package run.halo.app.notification.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.notification.NotifierDescriptor;
import run.halo.app.core.extension.notification.ReasonType;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
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
        var tag = "NotificationV1alpha1Uc";
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
                        .implementation(ReasonTypeNotifierMatrix.class)
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
                .flatMap(result -> ServerResponse.ok().bodyValue(result)))
            );
    }

    private Mono<ServerResponse> listNotificationPreferences(ServerRequest request) {
        var username = request.pathVariable("username");
        return listReasonTypeNotifierMatrix(username)
            .flatMap(matrix -> ServerResponse.ok().bodyValue(matrix));
    }

    @NonNull
    private static <T> Map<String, Integer> toNameIndexMap(List<T> collection,
        Function<T, String> nameGetter) {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < collection.size(); i++) {
            var item = collection.get(i);
            indexMap.put(nameGetter.apply(item), i);
        }
        return indexMap;
    }

    Mono<ReasonTypeNotifierMatrix> listReasonTypeNotifierMatrix(String username) {
        var listOptions = ListOptions.builder()
            .labelSelector()
            .notExists(MetadataUtil.HIDDEN_LABEL)
            .end()
            .build();
        return client.listAll(ReasonType.class, listOptions, ExtensionUtil.defaultSort())
            .map(ReasonTypeInfo::from)
            .collectList()
            .flatMap(reasonTypes -> client.list(NotifierDescriptor.class, null,
                    Comparators.defaultComparator())
                .map(notifier -> new NotifierInfo(notifier.getMetadata().getName(),
                    notifier.getSpec().getDisplayName(),
                    notifier.getSpec().getDescription())
                )
                .collectList()
                .map(notifiers -> {
                    var matrix = new ReasonTypeNotifierMatrix()
                        .setReasonTypes(reasonTypes)
                        .setNotifiers(notifiers)
                        .setStateMatrix(new boolean[reasonTypes.size()][notifiers.size()]);
                    return Tuples.of(reasonTypes, matrix);
                })
            )
            .flatMap(tuple2 -> {
                var reasonTypes = tuple2.getT1();
                var matrix = tuple2.getT2();

                var reasonTypeIndexMap = toNameIndexMap(reasonTypes, ReasonTypeInfo::name);
                var notifierIndexMap = toNameIndexMap(matrix.getNotifiers(), NotifierInfo::name);
                var stateMatrix = matrix.getStateMatrix();

                return userNotificationPreferenceService.getByUser(username)
                    .doOnNext(preference -> {
                        var reasonTypeNotifierMap = preference.getReasonTypeNotifier();
                        for (ReasonTypeInfo reasonType : reasonTypes) {
                            var reasonTypeIndex = reasonTypeIndexMap.get(reasonType.name());
                            var notifierNames =
                                reasonTypeNotifierMap.getNotifiers(reasonType.name());
                            for (String notifierName : notifierNames) {
                                // Skip if the notifier enabled in the user preference does not
                                // exist to avoid null index
                                if (!notifierIndexMap.containsKey(notifierName)) {
                                    continue;
                                }
                                var notifierIndex = notifierIndexMap.get(notifierName);
                                stateMatrix[reasonTypeIndex][notifierIndex] = true;
                            }
                        }
                    })
                    .thenReturn(matrix);
            });
    }

    @Data
    @Accessors(chain = true)
    static class ReasonTypeNotifierMatrix {
        private List<ReasonTypeInfo> reasonTypes;
        private List<NotifierInfo> notifiers;
        private boolean[][] stateMatrix;
    }

    record ReasonTypeInfo(String name, String displayName, String description,
                          Set<String> uiPermissions) {

        public static ReasonTypeInfo from(ReasonType reasonType) {
            var uiPermissions = Optional.of(MetadataUtil.nullSafeAnnotations(reasonType))
                .map(annotations -> annotations.get(Role.UI_PERMISSIONS_ANNO))
                .filter(StringUtils::isNotBlank)
                .map(uiPermissionStr -> JsonUtils.jsonToObject(uiPermissionStr,
                    new TypeReference<Set<String>>() {
                    })
                )
                .orElse(Set.of());
            return new ReasonTypeInfo(reasonType.getMetadata().getName(),
                reasonType.getSpec().getDisplayName(),
                reasonType.getSpec().getDescription(),
                uiPermissions);
        }
    }

    record NotifierInfo(String name, String displayName, String description) {
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
