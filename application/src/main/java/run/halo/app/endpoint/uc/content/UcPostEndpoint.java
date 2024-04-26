package run.halo.app.endpoint.uc.content;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.content.Content;
import run.halo.app.content.ContentUpdateParam;
import run.halo.app.content.ListedPost;
import run.halo.app.content.PostQuery;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.SnapshotService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Ref;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.JsonUtils;

@Component
public class UcPostEndpoint implements CustomEndpoint {

    private static final String CONTENT_JSON_ANNO = "content.halo.run/content-json";

    private final PostService postService;

    private final SnapshotService snapshotService;

    public UcPostEndpoint(PostService postService, SnapshotService snapshotService) {
        this.postService = postService;
        this.snapshotService = snapshotService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = groupVersion() + "/Post";
        var namePathParam = parameterBuilder().name("name")
            .description("Post name")
            .in(ParameterIn.PATH)
            .required(true);
        return route().nest(
                path("/posts"),
                () -> route()
                    .GET(this::listMyPost, builder -> {
                            builder.operationId("ListMyPosts")
                                .description("List posts owned by the current user.")
                                .tag(tag)
                                .response(responseBuilder().implementation(
                                    ListResult.generateGenericClass(ListedPost.class)));
                            PostQuery.buildParameters(builder);
                        }
                    )
                    .POST(this::createMyPost, builder -> builder.operationId("CreateMyPost")
                        .tag(tag)
                        .description("""
                            Create my post. If you want to create a post with content, please set
                             annotation: "content.halo.run/content-json" into annotations and refer
                             to Content for corresponding data type.
                            """)
                        .requestBody(requestBodyBuilder().implementation(Post.class))
                        .response(responseBuilder().implementation(Post.class))
                    )
                    .GET("/{name}", this::getMyPost, builder -> builder.operationId("GetMyPost")
                        .tag(tag)
                        .parameter(namePathParam)
                        .description("Get post that belongs to the current user.")
                        .response(responseBuilder().implementation(Post.class))
                    )
                    .PUT("/{name}", this::updateMyPost, builder ->
                        builder.operationId("UpdateMyPost")
                            .tag(tag)
                            .parameter(namePathParam)
                            .description("Update my post.")
                            .requestBody(requestBodyBuilder().implementation(Post.class))
                            .response(responseBuilder().implementation(Post.class))
                    )
                    .GET("/{name}/draft", this::getMyPostDraft, builder -> builder.tag(tag)
                        .operationId("GetMyPostDraft")
                        .description("Get my post draft.")
                        .parameter(namePathParam)
                        .parameter(parameterBuilder()
                            .name("patched")
                            .in(ParameterIn.QUERY)
                            .required(false)
                            .implementation(Boolean.class)
                            .description("Should include patched content and raw or not.")
                        )
                        .response(responseBuilder().implementation(Snapshot.class))
                    )
                    .PUT("/{name}/draft", this::updateMyPostDraft, builder -> builder.tag(tag)
                        .operationId("UpdateMyPostDraft")
                        .description("""
                            Update draft of my post. Please make sure set annotation: 
                            "content.halo.run/content-json" into annotations and refer to 
                            Content for corresponding data type.
                             """)
                        .parameter(namePathParam)
                        .requestBody(requestBodyBuilder().implementation(Snapshot.class))
                        .response(responseBuilder().implementation(Snapshot.class)))
                    .PUT("/{name}/publish", this::publishMyPost, builder -> builder.tag(tag)
                        .operationId("PublishMyPost")
                        .description("Publish my post.")
                        .parameter(namePathParam)
                        .response(responseBuilder().implementation(Post.class)))
                    .PUT("/{name}/unpublish", this::unpublishMyPost, builder -> builder.tag(tag)
                        .operationId("UnpublishMyPost")
                        .description("Unpublish my post.")
                        .parameter(namePathParam)
                        .response(responseBuilder().implementation(Post.class)))
                    .build(),
                builder -> {
                })
            .build();
    }

    private Mono<ServerResponse> getMyPostDraft(ServerRequest request) {
        var name = request.pathVariable("name");
        var patched = request.queryParam("patched").map(Boolean::valueOf).orElse(false);
        var draft = getMyPost(name)
            .flatMap(post -> {
                var headSnapshotName = post.getSpec().getHeadSnapshot();
                var baseSnapshotName = post.getSpec().getBaseSnapshot();
                if (StringUtils.isBlank(headSnapshotName)) {
                    headSnapshotName = baseSnapshotName;
                }
                if (patched) {
                    return snapshotService.getPatchedBy(headSnapshotName, baseSnapshotName);
                }
                return snapshotService.getBy(headSnapshotName);
            });
        return ServerResponse.ok().body(draft, Snapshot.class);
    }

    private Mono<ServerResponse> unpublishMyPost(ServerRequest request) {
        var name = request.pathVariable("name");
        var postMono = getCurrentUser()
            .flatMap(username -> postService.getByUsername(name, username));
        var unpublishedPost = postMono.flatMap(postService::unpublish);
        return ServerResponse.ok().body(unpublishedPost, Post.class);
    }

    private Mono<ServerResponse> publishMyPost(ServerRequest request) {
        var name = request.pathVariable("name");
        var postMono = getCurrentUser()
            .flatMap(username -> postService.getByUsername(name, username));

        var publishedPost = postMono.flatMap(postService::publish);
        return ServerResponse.ok().body(publishedPost, Post.class);
    }

    private Mono<ServerResponse> updateMyPostDraft(ServerRequest request) {
        var name = request.pathVariable("name");
        var postMono = getMyPost(name).cache();
        var snapshotMono = request.bodyToMono(Snapshot.class).cache();

        var contentMono = snapshotMono
            .map(Snapshot::getMetadata)
            .filter(metadata -> {
                var annotations = metadata.getAnnotations();
                return annotations != null && annotations.containsKey(CONTENT_JSON_ANNO);
            })
            .map(metadata -> {
                var contentJson = metadata.getAnnotations().remove(CONTENT_JSON_ANNO);
                return JsonUtils.jsonToObject(contentJson, Content.class);
            })
            .cache();

        // check the snapshot belongs to the post.
        var checkSnapshot = postMono.flatMap(post -> snapshotMono.filter(
                snapshot -> Ref.equals(snapshot.getSpec().getSubjectRef(), post)
            ).switchIfEmpty(Mono.error(() ->
                new ServerWebInputException("The snapshot does not belong to the given post."))
            ).filter(snapshot -> {
                var snapshotName = snapshot.getMetadata().getName();
                var headSnapshotName = post.getSpec().getHeadSnapshot();
                return Objects.equals(snapshotName, headSnapshotName);
            }).switchIfEmpty(Mono.error(() ->
                new ServerWebInputException("The snapshot was not the head snapshot of the post.")))
        ).then();

        var setContributor = getCurrentUser().flatMap(username ->
            snapshotMono.doOnNext(snapshot -> Snapshot.addContributor(snapshot, username)));

        var getBaseSnapshot = postMono.map(post -> post.getSpec().getBaseSnapshot())
            .flatMap(snapshotService::getBy);

        var updatedSnapshot = getBaseSnapshot.flatMap(
            baseSnapshot -> contentMono.flatMap(content -> postMono.flatMap(post -> {
                var postName = post.getMetadata().getName();
                var headSnapshotName = post.getSpec().getHeadSnapshot();
                var releaseSnapshotName = post.getSpec().getReleaseSnapshot();
                if (!Objects.equals(headSnapshotName, releaseSnapshotName)) {
                    // patch and update
                    return snapshotMono.flatMap(
                        s -> snapshotService.patchAndUpdate(s, baseSnapshot, content));
                }
                // patch and create
                return getCurrentUser().map(
                        username -> {
                            var metadata = new Metadata();
                            metadata.setGenerateName(postName + "-snapshot-");
                            var spec = new Snapshot.SnapShotSpec();
                            spec.setParentSnapshotName(headSnapshotName);
                            spec.setOwner(username);
                            spec.setSubjectRef(Ref.of(post));

                            var snapshot = new Snapshot();
                            snapshot.setMetadata(metadata);
                            snapshot.setSpec(spec);
                            Snapshot.addContributor(snapshot, username);
                            return snapshot;
                        })
                    .flatMap(s -> snapshotService.patchAndCreate(s, baseSnapshot, content))
                    .flatMap(createdSnapshot -> {
                        post.getSpec().setHeadSnapshot(createdSnapshot.getMetadata().getName());
                        return postService.updateBy(post).thenReturn(createdSnapshot);
                    });
            })));

        return ServerResponse.ok()
            .body(checkSnapshot.and(setContributor).then(updatedSnapshot), Snapshot.class);
    }

    private Mono<ServerResponse> updateMyPost(ServerRequest request) {
        var name = request.pathVariable("name");

        var postBody = request.bodyToMono(Post.class)
            .doOnNext(post -> {
                var annotations = post.getMetadata().getAnnotations();
                if (annotations != null) {
                    // we don't support updating content while updating post.
                    annotations.remove(CONTENT_JSON_ANNO);
                }
            })
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required.")));

        var updatedPost = getMyPost(name).flatMap(oldPost ->
                postBody.doOnNext(post -> {
                    var oldSpec = oldPost.getSpec();
                    // restrict fields of post.spec.
                    var spec = post.getSpec();
                    spec.setOwner(oldSpec.getOwner());
                    spec.setPublish(oldSpec.getPublish());
                    spec.setHeadSnapshot(oldSpec.getHeadSnapshot());
                    spec.setBaseSnapshot(oldSpec.getBaseSnapshot());
                    spec.setReleaseSnapshot(oldSpec.getReleaseSnapshot());
                    spec.setDeleted(oldSpec.getDeleted());
                    post.getMetadata().setName(oldPost.getMetadata().getName());
                }))
            .flatMap(postService::updateBy);
        return ServerResponse.ok().body(updatedPost, Post.class);
    }

    private Mono<ServerResponse> createMyPost(ServerRequest request) {
        var postFromRequest = request.bodyToMono(Post.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required.")));

        var createdPost = getCurrentUser()
            .flatMap(username -> postFromRequest
                .doOnNext(post -> {
                    if (post.getSpec() == null) {
                        post.setSpec(new Post.PostSpec());
                    }
                    post.getSpec().setOwner(username);
                }))
            .map(post -> new PostRequest(post, ContentUpdateParam.from(getContent(post))))
            .flatMap(postService::draftPost);
        return ServerResponse.ok().body(createdPost, Post.class);
    }

    private Content getContent(Post post) {
        Content content = null;
        var annotations = post.getMetadata().getAnnotations();
        if (annotations != null && annotations.containsKey(CONTENT_JSON_ANNO)) {
            var contentJson = annotations.remove(CONTENT_JSON_ANNO);
            content = JsonUtils.jsonToObject(contentJson, Content.class);
        }
        return content;
    }

    private Mono<ServerResponse> listMyPost(ServerRequest request) {
        var posts = getCurrentUser()
            .map(username -> new PostQuery(request, username))
            .flatMap(postService::listPost);
        return ServerResponse.ok().body(posts, ListedPost.class);
    }

    private Mono<ServerResponse> getMyPost(ServerRequest request) {
        var postName = request.pathVariable("name");
        var post = getMyPost(postName);
        return ServerResponse.ok().body(post, Post.class);
    }

    private Mono<Post> getMyPost(String postName) {
        return getCurrentUser()
            .flatMap(username -> postService.getByUsername(postName, username)
                .switchIfEmpty(
                    Mono.error(() -> new NotFoundException("The post was not found or deleted"))
                )
            );
    }

    private Mono<String> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.content.halo.run/v1alpha1");
    }

}
