package run.halo.app.core.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;

@Component
@RequiredArgsConstructor
public class SystemConfigEndpoint implements CustomEndpoint {
    private final SystemConfigurableEnvironmentFetcher configurableEnvironmentFetcher;

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
            .build();
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
