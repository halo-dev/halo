package run.halo.app.search;

import static run.halo.app.core.extension.Post.PostPhase.PUBLISHED;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerErrorException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.post.PostDoc;
import run.halo.app.search.post.PostSearchService;
import run.halo.app.theme.finders.PostFinder;

@Component
public class IndicesEndpoint implements CustomEndpoint {

    private final PostFinder postFinder;

    private final ExtensionGetter extensionGetter;

    private static final String API_VERSION = "api.console.halo.run/v1alpha1";

    public IndicesEndpoint(PostFinder postFinder,
        ExtensionGetter extensionGetter) {
        this.postFinder = postFinder;
        this.extensionGetter = extensionGetter;
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
                    // TODO Optimize listing posts with non-blocking.
                    return postFinder.list(0, 0)
                        .stream()
                        .filter(post -> PUBLISHED.name().equals(post.getStatus().getPhase()))
                        .peek(post -> postFinder.content(post.getMetadata().getName()))
                        .map(PostDoc::from)
                        .toList();
                })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(posts -> extensionGetter.getEnabledExtension(PostSearchService.class)
                .switchIfEmpty(Mono.error(() ->
                    new ServerErrorException("Please select any post search service before "
                        + "rebuilding index", null)))
                .doOnNext(searchService -> {
                    try {
                        searchService.addDocuments(posts);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .thenReturn(posts))
            .flatMap(posts -> ServerResponse.ok()
                .bodyValue("Rebuilt post indices for " + posts.size() + " post(s)")
            );
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(API_VERSION);
    }
}
