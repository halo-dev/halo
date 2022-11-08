package run.halo.app.search;

import static run.halo.app.core.extension.Post.PostPhase.PUBLISHED;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.search.post.PostDoc;
import run.halo.app.search.post.PostSearchService;
import run.halo.app.theme.finders.PostFinder;

@Component
public class IndicesEndpoint implements CustomEndpoint {

    private final PostSearchService postSearchService;

    private final PostFinder postFinder;

    private static final String API_VERSION = "api.console.halo.run/v1alpha1";

    public IndicesEndpoint(PostSearchService postSearchService, PostFinder postFinder) {
        this.postSearchService = postSearchService;
        this.postFinder = postFinder;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = API_VERSION + "/Indices";
        return SpringdocRouteBuilder.route()
            .POST("indices/post", this::rebuildPostIndices,
                builder -> builder.operationId("BuildPostIndices")
                    .tag(tag)
                    .description("Build or rebuild post indices for full text search"))
            .build();
    }

    private Mono<ServerResponse> rebuildPostIndices(ServerRequest request) {
        return Mono.fromCallable(
                () -> {
                    // TODO Optimize listing posts.
                    var posts = postFinder.list(0, 0)
                        .stream()
                        .filter(post -> PUBLISHED.name().equals(post.getStatus().getPhase()))
                        .peek(post -> postFinder.content(post.getMetadata().getName()))
                        .map(PostDoc::from)
                        .toList();
                    postSearchService.addDocuments(posts);
                    return posts;
                })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(posts -> ServerResponse.ok()
                .bodyValue("Rebuilt post indices for " + posts.size() + " post(s)")
            );
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(API_VERSION);
    }
}
