package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class SystemConfigEndpoint implements CustomEndpoint {
    private final SystemConfigurableEnvironmentFetcher configurableEnvironmentFetcher;
    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "SystemConfigV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .GET("/systemconfigs/{group}", this::getConfigByGroup,
                builder -> builder.operationId("getSystemConfigByGroup")
                    .description("Get system config by group")
                    .tag(tag)
                    .response(responseBuilder()
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                        )
                        .implementation(ObjectNode.class))
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("group")
                        .required(true)
                        .description("Group of the system config")
                    )
            )
            .PUT("/systemconfigs/{group}", this::updateConfigByGroup,
                builder -> builder.operationId("updateSystemConfigByGroup")
                    .description("Update system config by group")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("group")
                        .required(true)
                        .description("Group of the system config")
                    )
                    .requestBody(requestBodyBuilder()
                        .implementation(ObjectNode.class)
                    )
                    .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.NO_CONTENT))
                        .implementation(Void.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> updateConfigByGroup(ServerRequest request) {
        final var group = request.pathVariable("group");
        return request.bodyToMono(ObjectNode.class)
            .flatMap(objectNode -> configurableEnvironmentFetcher.loadConfigMap()
                .flatMap(configMap -> {
                    var data = configMap.getData();
                    data.put(group, JsonUtils.objectToJson(objectNode));
                    return client.update(configMap);
                })
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance))
            .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> getConfigByGroup(ServerRequest request) {
        final var group = request.pathVariable("group");
        return configurableEnvironmentFetcher.fetch(group, ObjectNode.class)
            .switchIfEmpty(Mono.fromSupplier(JsonNodeFactory.instance::objectNode))
            .flatMap(json -> ServerResponse.ok().bodyValue(json));
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("console.api.halo.run", "v1alpha1");
    }
}
