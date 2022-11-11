package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.ListedPost;
import run.halo.app.content.PostQuery;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.Post;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostRecycledEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.QueryParamBuildUtil;

/**
 * Endpoint for managing posts.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class PostEndpoint implements CustomEndpoint {

    private final PostService postService;
    private final ReactiveExtensionClient client;

    private final ApplicationEventPublisher eventPublisher;

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
                    QueryParamBuildUtil.buildParametersFromType(builder, PostQuery.class);
                }
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
            .build();
    }

    Mono<ServerResponse> draftPost(ServerRequest request) {
        return request.bodyToMono(PostRequest.class)
            .flatMap(postService::draftPost)
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> updatePost(ServerRequest request) {
        return request.bodyToMono(PostRequest.class)
            .flatMap(postService::updatePost)
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> publishPost(ServerRequest request) {
        var name = request.pathVariable("name");
        return client.get(Post.class, name)
            .doOnNext(post -> {
                var spec = post.getSpec();
                request.queryParam("headSnapshot").ifPresent(spec::setHeadSnapshot);
                spec.setPublish(true);
                // TODO Provide release snapshot query param to control
                spec.setReleaseSnapshot(spec.getHeadSnapshot());
            })
            .flatMap(client::update)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            .flatMap(post -> postService.publishPost(post.getMetadata().getName()))
            // TODO Fire published event in reconciler in the future
            .doOnNext(post -> eventPublisher.publishEvent(
                new PostPublishedEvent(this, post.getMetadata().getName())))
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    private Mono<ServerResponse> unpublishPost(ServerRequest request) {
        var name = request.pathVariable("name");
        return client.get(Post.class, name)
            .doOnNext(post -> {
                var spec = post.getSpec();
                spec.setPublish(false);
            })
            .flatMap(client::update)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            // TODO Fire unpublished event in reconciler in the future
            .doOnNext(post -> eventPublisher.publishEvent(
                new PostUnpublishedEvent(this, post.getMetadata().getName())))
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    private Mono<ServerResponse> recyclePost(ServerRequest request) {
        var name = request.pathVariable("name");
        return client.get(Post.class, name)
            .doOnNext(post -> {
                var spec = post.getSpec();
                spec.setDeleted(true);
            })
            .flatMap(client::update)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(t -> t instanceof OptimisticLockingFailureException))
            // TODO Fire recycled event in reconciler in the future
            .doOnNext(post -> eventPublisher.publishEvent(
                new PostRecycledEvent(this, post.getMetadata().getName())))
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> listPost(ServerRequest request) {
        PostQuery postQuery = new PostQuery(request.queryParams());
        return postService.listPost(postQuery)
            .flatMap(listedPosts -> ServerResponse.ok().bodyValue(listedPosts));
    }
}
