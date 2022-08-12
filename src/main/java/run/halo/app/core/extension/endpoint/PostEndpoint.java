package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ExtensionClient;

/**
 * Endpoint for managing posts.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostEndpoint implements CustomEndpoint{

    private final ExtensionClient client;

    public PostEndpoint(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "content.halo.run/v1alpha1/Post";
        return SpringdocRouteBuilder.route()
            .POST("posts", contentType(MediaType.APPLICATION_JSON),
                null, builder -> builder.operationId("DraftPost")
                    .description("Draft a post.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(ThemeEndpoint.InstallRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Theme.class))
            )
            .build();
    }
}
