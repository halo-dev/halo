package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static run.halo.app.extension.MetadataUtil.nullSafeLabels;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.Content;
import run.halo.app.content.ContentUpdateParam;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.ListedPost;
import run.halo.app.content.ListedSnapshotDto;
import run.halo.app.content.PostQuery;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Endpoint for managing posts.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostEndpoint implements CustomEndpoint {

    private int maxAttemptsWaitForPublish = 10;
    private final PostService postService;
    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/Post";
        return SpringdocRouteBuilder.route()
            .GET("posts", this::listPost, builder -> {
                    builder.operationId("ListPosts")
                        .description("List posts.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedPost.class))
                        );
                    PostQuery.buildParameters(builder);
                }
            )
            .GET("posts/{name}/head-content", this::fetchHeadContent,
                builder -> builder.operationId("fetchPostHeadContent")
                    .description("Fetch head content of post.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .GET("posts/{name}/content", this::fetchContent,
                builder -> builder.operationId("fetchPostContent")
                    .description("Fetch content of post.")
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
            .GET("posts/{name}/release-content", this::fetchReleaseContent,
                builder -> builder.operationId("fetchPostReleaseContent")
                    .description("Fetch release content of post.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .GET("posts/{name}/snapshot", this::listSnapshots,
                builder -> builder.operationId("listPostSnapshots")
                    .description("List all snapshots for post content.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .response(responseBuilder()
                        .implementationArray(ListedSnapshotDto.class))
            )
            .POST("posts", this::draftPost,
                builder -> builder.operationId("DraftPost")
                    .description("Draft a post.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(PostRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Post.class))
            )
            .PUT("posts/{name}", this::updatePost,
                builder -> builder.operationId("UpdateDraftPost")
                    .description("Update a post.")
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
                                .implementation(PostRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Post.class))
            )
            .PUT("posts/{name}/content", this::updateContent,
                builder -> builder.operationId("UpdatePostContent")
                    .description("Update a post's content.")
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
            .PUT("posts/{name}/revert-content", this::revertToSpecifiedSnapshot,
                builder -> builder.operationId("revertToSpecifiedSnapshotForPost")
                    .description("Revert to specified snapshot for post content.")
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
            .PUT("posts/{name}/publish", this::publishPost,
                builder -> builder.operationId("PublishPost")
                    .description("Publish a post.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class))
                    .parameter(parameterBuilder().name("headSnapshot")
                        .description("Head snapshot name of content.")
                        .in(ParameterIn.QUERY)
                        .required(false))
                    .parameter(parameterBuilder()
                        .name("async")
                        .in(ParameterIn.QUERY)
                        .implementation(Boolean.class)
                        .required(false))
                    .response(responseBuilder()
                        .implementation(Post.class))
            )
            .PUT("posts/{name}/unpublish", this::unpublishPost,
                builder -> builder.operationId("UnpublishPost")
                    .description("Publish a post.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true))
                    .response(responseBuilder()
                        .implementation(Post.class)))
            .PUT("posts/{name}/recycle", this::recyclePost,
                builder -> builder.operationId("RecyclePost")
                    .description("Recycle a post.")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name")
                        .in(ParameterIn.PATH)
                        .required(true)))
            .DELETE("posts/{name}/content", this::deleteContent,
                builder -> builder.operationId("deletePostContent")
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
        return postService.deleteContent(postName, snapshotName)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> revertToSpecifiedSnapshot(ServerRequest request) {
        final var postName = request.pathVariable("name");
        return request.bodyToMono(RevertSnapshotParam.class)
            .switchIfEmpty(
                Mono.error(new ServerWebInputException("Required request body is missing.")))
            .flatMap(param -> postService.revertToSpecifiedSnapshot(postName, param.snapshotName))
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    @Schema(name = "RevertSnapshotForPostParam")
    record RevertSnapshotParam(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1) String snapshotName) {
    }

    private Mono<ServerResponse> fetchContent(ServerRequest request) {
        final var name = request.pathVariable("name");
        final var snapshotName = request.queryParam("snapshotName").orElseThrow();
        return client.fetch(Post.class, name)
            .flatMap(post -> {
                var baseSnapshot = post.getSpec().getBaseSnapshot();
                return postService.getContent(snapshotName, baseSnapshot);
            })
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> listSnapshots(ServerRequest request) {
        String name = request.pathVariable("name");
        var resultFlux = postService.listSnapshots(name);
        return ServerResponse.ok().body(resultFlux, ListedSnapshotDto.class);
    }

    private Mono<ServerResponse> fetchReleaseContent(ServerRequest request) {
        final var name = request.pathVariable("name");
        return postService.getReleaseContent(name)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> fetchHeadContent(ServerRequest request) {
        String name = request.pathVariable("name");
        return postService.getHeadContent(name)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    Mono<ServerResponse> draftPost(ServerRequest request) {
        return request.bodyToMono(PostRequest.class)
            .flatMap(postService::draftPost)
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> updateContent(ServerRequest request) {
        String postName = request.pathVariable("name");
        return request.bodyToMono(ContentUpdateParam.class)
            .flatMap(content -> Mono.defer(() -> client.fetch(Post.class, postName)
                    .flatMap(post -> {
                        PostRequest postRequest = new PostRequest(post, content);
                        return postService.updatePost(postRequest);
                    }))
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                    .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            )
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> updatePost(ServerRequest request) {
        return request.bodyToMono(PostRequest.class)
            .flatMap(postService::updatePost)
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> publishPost(ServerRequest request) {
        var name = request.pathVariable("name");
        boolean asyncPublish = request.queryParam("async")
            .map(Boolean::parseBoolean)
            .orElse(false);

        return Mono.defer(() -> client.get(Post.class, name)
                .doOnNext(post -> {
                    var spec = post.getSpec();
                    request.queryParam("headSnapshot").ifPresent(spec::setHeadSnapshot);
                    spec.setPublish(true);
                    if (spec.getHeadSnapshot() == null) {
                        spec.setHeadSnapshot(spec.getBaseSnapshot());
                    }
                    spec.setReleaseSnapshot(spec.getHeadSnapshot());
                })
                .flatMap(client::update)
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            .filter(post -> asyncPublish)
            .switchIfEmpty(Mono.defer(() -> awaitPostPublished(name)))
            .onErrorMap(Exceptions::isRetryExhausted, err -> new ServerErrorException(
                "Post publishing failed, please try again later.", err))
            .flatMap(publishResult -> ServerResponse.ok().bodyValue(publishResult));
    }

    private Mono<Post> awaitPostPublished(String postName) {
        Predicate<Post> schedulePublish = post -> {
            var labels = nullSafeLabels(post);
            return BooleanUtils.TRUE.equals(labels.get(Post.SCHEDULING_PUBLISH_LABEL));
        };
        return Mono.defer(() -> client.get(Post.class, postName)
                .filter(post -> {
                    var releasedSnapshot = MetadataUtil.nullSafeAnnotations(post)
                        .get(Post.LAST_RELEASED_SNAPSHOT_ANNO);
                    var expectReleaseSnapshot = post.getSpec().getReleaseSnapshot();
                    return Objects.equals(releasedSnapshot, expectReleaseSnapshot)
                        || schedulePublish.test(post);
                })
                .switchIfEmpty(Mono.error(
                    () -> new RetryException("Retry to check post publish status"))))
            .retryWhen(Retry.backoff(maxAttemptsWaitForPublish, Duration.ofMillis(100))
                .filter(t -> t instanceof RetryException));
    }

    private Mono<ServerResponse> unpublishPost(ServerRequest request) {
        var name = request.pathVariable("name");
        return Mono.defer(() -> client.get(Post.class, name)
                .doOnNext(post -> {
                    var spec = post.getSpec();
                    spec.setPublish(false);
                })
                .flatMap(client::update))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    private Mono<ServerResponse> recyclePost(ServerRequest request) {
        var name = request.pathVariable("name");
        return Mono.defer(() -> client.get(Post.class, name)
                .doOnNext(post -> {
                    var spec = post.getSpec();
                    spec.setDeleted(true);
                })
                .flatMap(client::update))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> listPost(ServerRequest request) {
        PostQuery postQuery = new PostQuery(request);
        return postService.listPost(postQuery)
            .flatMap(listedPosts -> ServerResponse.ok().bodyValue(listedPosts));
    }

    /**
     * Convenient for testing, to avoid waiting too long for post published when testing.
     */
    public void setMaxAttemptsWaitForPublish(int maxAttempts) {
        this.maxAttemptsWaitForPublish = maxAttempts;
    }
}
