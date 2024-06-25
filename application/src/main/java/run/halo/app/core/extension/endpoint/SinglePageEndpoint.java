package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.Content;
import run.halo.app.content.ContentUpdateParam;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.ListedSinglePage;
import run.halo.app.content.ListedSnapshotDto;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.SinglePageRequest;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Endpoint for managing {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class SinglePageEndpoint implements CustomEndpoint {

    private final SinglePageService singlePageService;
    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "SinglePageV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .GET("singlepages", this::listSinglePage, builder -> {
                    builder.operationId("ListSinglePages")
                        .description("List single pages.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedSinglePage.class))
                        );
                    SinglePageQuery.buildParameters(builder);
                }
            )
            .GET("singlepages/{name}/head-content", this::fetchHeadContent,
                builder -> builder.operationId("fetchSinglePageHeadContent")
                    .description("Fetch head content of single page.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .GET("singlepages/{name}/release-content", this::fetchReleaseContent,
                builder -> builder.operationId("fetchSinglePageReleaseContent")
                    .description("Fetch release content of single page.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .GET("singlepages/{name}/content", this::fetchContent,
                builder -> builder.operationId("fetchSinglePageContent")
                    .description("Fetch content of single page.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .parameter(parameterBuilder().name("snapshotName")
                        .in(ParameterIn.QUERY)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .GET("singlepages/{name}/snapshot", this::listSnapshots,
                builder -> builder.operationId("listSinglePageSnapshots")
                    .description("List all snapshots for single page content.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementationArray(ListedSnapshotDto.class))
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
            .PUT("singlepages/{name}/content", this::updateContent,
                builder -> builder.operationId("UpdateSinglePageContent")
                    .description("Update a single page's content.")
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
                                .implementation(Content.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Post.class))
            )
            .PUT("singlepages/{name}/revert-content", this::revertToSpecifiedSnapshot,
                builder -> builder.operationId("revertToSpecifiedSnapshotForSinglePage")
                    .description("Revert to specified snapshot for single page content.")
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
                                .implementation(RevertSnapshotParam.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Post.class))
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
            .DELETE("singlepages/{name}/content", this::deleteContent,
                builder -> builder.operationId("deleteSinglePageContent")
                    .description("Delete a content for post.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .parameter(parameterBuilder()
                        .name("snapshotName")
                        .in(ParameterIn.QUERY)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .build();
    }

    private Mono<ServerResponse> deleteContent(ServerRequest request) {
        final var postName = request.pathVariable("name");
        final var snapshotName = request.queryParam("snapshotName").orElseThrow();
        return singlePageService.deleteContent(postName, snapshotName)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> revertToSpecifiedSnapshot(ServerRequest request) {
        final var postName = request.pathVariable("name");
        return request.bodyToMono(RevertSnapshotParam.class)
            .switchIfEmpty(
                Mono.error(new ServerWebInputException("Required request body is missing.")))
            .flatMap(
                param -> singlePageService.revertToSpecifiedSnapshot(postName, param.snapshotName))
            .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    @Schema(name = "RevertSnapshotForSingleParam")
    record RevertSnapshotParam(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1) String snapshotName) {
    }

    private Mono<ServerResponse> fetchContent(ServerRequest request) {
        final var snapshotName = request.queryParam("snapshotName").orElseThrow();
        return client.fetch(SinglePage.class, request.pathVariable("name"))
            .flatMap(page -> {
                var baseSnapshot = page.getSpec().getBaseSnapshot();
                return singlePageService.getContent(snapshotName, baseSnapshot);
            })
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> listSnapshots(ServerRequest request) {
        final var name = request.pathVariable("name");
        var resultFlux = singlePageService.listSnapshots(name);
        return ServerResponse.ok().body(resultFlux, ListedSnapshotDto.class);
    }

    private Mono<ServerResponse> fetchReleaseContent(ServerRequest request) {
        final var name = request.pathVariable("name");
        return singlePageService.getReleaseContent(name)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> fetchHeadContent(ServerRequest request) {
        String name = request.pathVariable("name");
        return singlePageService.getHeadContent(name)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    Mono<ServerResponse> draftSinglePage(ServerRequest request) {
        return request.bodyToMono(SinglePageRequest.class)
            .flatMap(singlePageService::draft)
            .flatMap(singlePage -> ServerResponse.ok().bodyValue(singlePage));
    }

    Mono<ServerResponse> updateContent(ServerRequest request) {
        String pageName = request.pathVariable("name");
        return request.bodyToMono(ContentUpdateParam.class)
            .flatMap(content -> Mono.defer(() -> client.fetch(SinglePage.class, pageName)
                    .flatMap(page -> {
                        SinglePageRequest pageRequest = new SinglePageRequest(page, content);
                        return singlePageService.update(pageRequest);
                    }))
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                    .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            )
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> updateSinglePage(ServerRequest request) {
        return request.bodyToMono(SinglePageRequest.class)
            .flatMap(singlePageService::update)
            .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    Mono<ServerResponse> publishSinglePage(ServerRequest request) {
        String name = request.pathVariable("name");
        boolean asyncPublish = request.queryParam("async")
            .map(Boolean::parseBoolean)
            .orElse(false);
        return Mono.defer(() -> client.get(SinglePage.class, name)
                .flatMap(singlePage -> {
                    SinglePage.SinglePageSpec spec = singlePage.getSpec();
                    spec.setPublish(true);
                    if (spec.getHeadSnapshot() == null) {
                        spec.setHeadSnapshot(spec.getBaseSnapshot());
                    }
                    spec.setReleaseSnapshot(spec.getHeadSnapshot());
                    return client.update(singlePage);
                })
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            .flatMap(post -> {
                if (asyncPublish) {
                    return Mono.just(post);
                }
                return client.fetch(SinglePage.class, name)
                    .map(latest -> {
                        String latestReleasedSnapshotName =
                            MetadataUtil.nullSafeAnnotations(latest)
                                .get(Post.LAST_RELEASED_SNAPSHOT_ANNO);
                        if (StringUtils.equals(latestReleasedSnapshotName,
                            latest.getSpec().getReleaseSnapshot())) {
                            return latest;
                        }
                        throw new RetryException("SinglePage publishing status is not as expected");
                    })
                    .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                        .filter(t -> t instanceof RetryException))
                    .doOnError(IllegalStateException.class, err -> {
                        log.error("Failed to publish single page [{}]", name, err);
                        throw new IllegalStateException("Publishing wait timeout.");
                    });
            })
            .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    Mono<ServerResponse> listSinglePage(ServerRequest request) {
        var listRequest = new SinglePageQuery(request);
        return singlePageService.list(listRequest)
            .flatMap(listedPages -> ServerResponse.ok().bodyValue(listedPages));
    }
}
