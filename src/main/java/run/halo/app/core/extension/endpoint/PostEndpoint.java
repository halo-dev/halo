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
import run.halo.app.content.ListedPost;
import run.halo.app.content.PostQuery;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;

/**
 * Endpoint for managing posts.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostEndpoint implements CustomEndpoint {

    private final PostService postService;

    public PostEndpoint(PostService postService) {
        this.postService = postService;
    }

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
                    .response(responseBuilder()
                        .implementation(Post.class))
            )
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
        String name = request.pathVariable("name");
        return postService.publishPost(name)
            .flatMap(post -> ServerResponse.ok().bodyValue(post));
    }

    Mono<ServerResponse> listPost(ServerRequest request) {
        PostQuery postQuery = new PostQuery(request.queryParams());
        return postService.listPost(postQuery)
            .flatMap(listedPosts -> ServerResponse.ok().bodyValue(listedPosts));
    }
}
