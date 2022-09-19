package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.content.ListedSinglePage;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.SinglePageRequest;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;

/**
 * Endpoint for managing {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SinglePageEndpoint implements CustomEndpoint {

    private final SinglePageService singlePageService;

    public SinglePageEndpoint(SinglePageService singlePageService) {
        this.singlePageService = singlePageService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/SinglePage";
        return SpringdocRouteBuilder.route()
            .GET("singlepages", this::listSinglePage, builder -> {
                    builder.operationId("ListSinglePages")
                        .description("List single pages.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedSinglePage.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, SinglePageQuery.class);
                }
            )
            .POST("singlepages", this::draftSinglePage,
                builder -> builder.operationId("DraftSinglePage")
                    .description("Draft a single page.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(SinglePageRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(SinglePage.class))
            )
            .PUT("singlepages/{name}", this::updateSinglePage,
                builder -> builder.operationId("UpdateDraftSinglePage")
                    .description("Update a single page.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(SinglePageRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(SinglePage.class))
            )
            .PUT("singlepages/{name}/publish", this::publishSinglePage,
                builder -> builder.operationId("PublishSinglePage")
                    .description("Publish a single page.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementation(SinglePage.class))
            )
            .build();
    }

    Mono<ServerResponse> draftSinglePage(ServerRequest request) {
        return request.bodyToMono(SinglePageRequest.class)
            .flatMap(singlePageService::draft)
            .flatMap(singlePage -> ServerResponse.ok().bodyValue(singlePage));
    }

    Mono<ServerResponse> updateSinglePage(ServerRequest request) {
        return request.bodyToMono(SinglePageRequest.class)
            .flatMap(singlePageService::update)
            .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    Mono<ServerResponse> publishSinglePage(ServerRequest request) {
        String name = request.pathVariable("name");
        return singlePageService.publish(name)
            .flatMap(singlePage -> ServerResponse.ok().bodyValue(singlePage));
    }

    Mono<ServerResponse> listSinglePage(ServerRequest request) {
        var listRequest = new SinglePageQuery(request.queryParams());
        return singlePageService.list(listRequest)
            .flatMap(listedPages -> ServerResponse.ok().bodyValue(listedPages));
    }
}
