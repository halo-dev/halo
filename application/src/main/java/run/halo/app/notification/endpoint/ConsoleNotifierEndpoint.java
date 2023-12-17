package run.halo.app.notification.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.notification.NotifierConfigStore;

/**
 * Custom notifier endpoint.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class ConsoleNotifierEndpoint implements CustomEndpoint {

    private final NotifierConfigStore notifierConfigStore;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/Notifier";
        return SpringdocRouteBuilder.route()
            .GET("/notifiers/{name}/sender-config", this::fetchSenderConfig,
                builder -> builder.operationId("FetchSenderConfig")
                    .description("Fetch sender config of notifier")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Notifier name")
                        .required(true)
                    )
                    .response(responseBuilder().implementation(ObjectNode.class))
            )
            .POST("/notifiers/{name}/sender-config", this::saveSenderConfig,
                builder -> builder.operationId("SaveSenderConfig")
                    .description("Save sender config of notifier")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Notifier name")
                        .required(true)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(ObjectNode.class))
                        )
                    )
                    .response(responseBuilder().implementation(Void.class))
            )
            .build();
    }

    private Mono<ServerResponse> fetchSenderConfig(ServerRequest request) {
        var name = request.pathVariable("name");
        return notifierConfigStore.fetchSenderConfig(name)
            .flatMap(config -> ServerResponse.ok().bodyValue(config));
    }

    private Mono<ServerResponse> saveSenderConfig(ServerRequest request) {
        var name = request.pathVariable("name");
        return request.bodyToMono(ObjectNode.class)
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("Request body must not be empty."))
            )
            .flatMap(jsonNode -> notifierConfigStore.saveSenderConfig(name, jsonNode))
            .then(ServerResponse.ok().build());
    }
}
