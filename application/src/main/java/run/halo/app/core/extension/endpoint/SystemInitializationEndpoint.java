package run.halo.app.core.extension.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemState;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.SuperAdminInitializer;

/**
 * System initialization endpoint.
 *
 * @author guqing
 * @since 2.9.0
 */
@Component
@RequiredArgsConstructor
public class SystemInitializationEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;
    private final SuperAdminInitializer superAdminInitializer;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/System";
        // define a non-resource api
        return SpringdocRouteBuilder.route()
            .POST("/system/initialize", this::initialize,
                builder -> builder.operationId("initialize")
                    .description("Initialize system")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .implementation(SystemInitializationRequest.class))
                    .response(responseBuilder().implementation(Boolean.class))
            )
            .build();
    }

    private Mono<ServerResponse> initialize(ServerRequest request) {
        return request.bodyToMono(SystemInitializationRequest.class)
            .switchIfEmpty(
                Mono.error(new ServerWebInputException("Request body must not be empty"))
            )
            .doOnNext(requestBody -> {
                if (!ValidationUtils.validateName(requestBody.getUsername())) {
                    throw new UnsatisfiedAttributeValueException(
                        "The username does not meet the specifications",
                        "problemDetail.user.username.unsatisfied", null);
                }
                if (StringUtils.isBlank(requestBody.getPassword())) {
                    throw new UnsatisfiedAttributeValueException(
                        "The password does not meet the specifications",
                        "problemDetail.user.password.unsatisfied", null);
                }
            })
            .flatMap(requestBody -> client.fetch(ConfigMap.class,
                    SystemState.SYSTEM_STATES_CONFIGMAP)
                .switchIfEmpty(Mono.defer(
                    () -> {
                        ConfigMap configMap = new ConfigMap();
                        configMap.setMetadata(new Metadata());
                        configMap.getMetadata()
                            .setName(SystemState.SYSTEM_STATES_CONFIGMAP);
                        configMap.setData(new HashMap<>());
                        return client.create(configMap);
                    })
                )
                .flatMap(config -> {
                    SystemState systemState = SystemState.deserialize(config);
                    if (isTrue(systemState.getIsSetup())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                            "System has been initialized"));
                    }
                    return initializeSystem(requestBody)
                        .then(Mono.defer(() -> {
                            systemState.setIsSetup(true);
                            SystemState.update(systemState, config);
                            return client.update(config);
                        }));
                })
            )
            .flatMap(config -> {
                SystemState systemState = SystemState.deserialize(config);
                return ServerResponse.ok().bodyValue(isTrue(systemState.getIsSetup()));
            });
    }

    private Mono<Void> initializeSystem(SystemInitializationRequest requestBody) {
        Mono<Void> initializeAdminUser = superAdminInitializer.initialize(
            requestBody.getUsername(),
            requestBody.getPassword(),
            requestBody.getEmail());

        Mono<Void> siteSetting =
            Mono.defer(() -> client.get(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
                    .flatMap(config -> {
                        Map<String, String> data = config.getData();
                        if (data == null) {
                            data = new LinkedHashMap<>();
                            config.setData(data);
                        }
                        String basic = data.getOrDefault(SystemSetting.Basic.GROUP, "{}");
                        SystemSetting.Basic basicSetting =
                            JsonUtils.jsonToObject(basic, SystemSetting.Basic.class);
                        basicSetting.setTitle(requestBody.getSiteTitle());
                        data.put(SystemSetting.Basic.GROUP, JsonUtils.objectToJson(basicSetting));
                        return client.update(config);
                    }))
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                    .filter(t -> t instanceof OptimisticLockingFailureException)
                )
                .then();
        return Mono.when(initializeAdminUser, siteSetting);
    }

    @Data
    public static class SystemInitializationRequest {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String username;

        @Schema(requiredMode = REQUIRED, minLength = 3)
        private String password;

        private String email;

        private String siteTitle;
    }
}
