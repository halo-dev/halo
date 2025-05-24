package run.halo.app.search;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

@Component
public class IndexEndpoint implements CustomEndpoint {

    private static final String API_VERSION = "api.halo.run/v1alpha1";

    private final SearchService searchService;

    public IndexEndpoint(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "IndexV1alpha1Public";
        return SpringdocRouteBuilder.route()
            .POST("/indices/-/search", this::indicesSearch,
                builder -> builder.operationId("IndicesSearch")
                    .tag(tag)
                    .description("Search indices.")
                    .requestBody(requestBodyBuilder().implementation(SearchOption.class)
                        .description("""
                            Please note that the "filterPublished", "filterExposed" and \
                            "filterRecycled" fields are ignored in this endpoint.\
                            """)
                    )
                    .response(responseBuilder().implementation(SearchResult.class))
            )
            .build();
    }

    private Mono<ServerResponse> indicesSearch(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SearchOption.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required.")))
            .flatMap(this::performSearch)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<SearchResult> performSearch(SearchOption option) {
        option.setFilterExposed(true);
        option.setFilterPublished(true);
        option.setFilterRecycled(false);
        return searchService.search(option);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(API_VERSION);
    }
}
