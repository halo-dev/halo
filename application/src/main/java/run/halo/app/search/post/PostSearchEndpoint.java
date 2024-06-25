package run.halo.app.search.post;

import static run.halo.app.infra.utils.GenericClassUtils.generateConcreteClass;

import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.SearchParam;
import run.halo.app.search.SearchResult;

@Component
public class PostSearchEndpoint implements CustomEndpoint {

    private static final String API_VERSION = "api.halo.run/v1alpha1";

    private final ExtensionGetter extensionGetter;

    public PostSearchEndpoint(ExtensionGetter extensionGetter) {
        this.extensionGetter = extensionGetter;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "PostV1alpha1Public";
        return SpringdocRouteBuilder.route()
            .GET("indices/post", this::search,
                builder -> {
                    builder.operationId("SearchPost")
                        .tag(tag)
                        .description("Search posts with fuzzy query")
                        .response(Builder.responseBuilder().implementation(
                            generateConcreteClass(SearchResult.class, PostHit.class,
                                () -> "PostHits")));
                    SearchParam.buildParameters(builder);
                }
            )
            .build();
    }

    private Mono<ServerResponse> search(ServerRequest request) {
        return Mono.fromSupplier(
                () -> new SearchParam(request.queryParams()))
            .flatMap(param -> extensionGetter.getEnabledExtension(PostSearchService.class)
                .switchIfEmpty(Mono.error(() ->
                    new RuntimeException("Please enable any post search service before searching")))
                .map(searchService -> {
                    try {
                        return searchService.search(param);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                }))
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(API_VERSION);
    }
}
