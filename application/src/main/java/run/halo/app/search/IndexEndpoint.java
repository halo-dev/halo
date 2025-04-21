package run.halo.app.search;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.util.List;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.search.post.PostHaloDocumentsProvider;

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
            .GET("/indices/post", this::search,
                builder -> {
                    builder.operationId("SearchPost")
                        .tag(tag)
                        .description(
                            "Search posts with fuzzy query. This method is deprecated, please use"
                                + " POST /indices/-/search instead.")
                        .deprecated(true)
                        .response(responseBuilder().implementation(SearchResult.class));
                    // Add search parameters directly instead of using deprecated SearchParam
                    builder.parameter(parameterBuilder()
                        .name("keyword")
                        .in(ParameterIn.QUERY)
                        .required(true)
                        .schema(schemaBuilder().implementation(String.class))
                        .description("Search keyword"))
                    .parameter(parameterBuilder()
                        .name("limit")
                        .in(ParameterIn.QUERY)
                        .schema(schemaBuilder().implementation(Integer.class).defaultValue("10"))
                        .description("Limit of search results, default is 10"));
                }
            )
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

    private Mono<ServerResponse> search(ServerRequest request) {
        return Mono.fromSupplier(() -> {
                var queryParams = request.queryParams();
                var option = new SearchOption();
                option.setIncludeTypes(List.of(PostHaloDocumentsProvider.POST_DOCUMENT_TYPE));

                // Extract parameters directly from query params
                String keyword = queryParams.getFirst("keyword");
                if (!StringUtils.hasText(keyword)) {
                    throw new ServerWebInputException("keyword is required");
                }
                option.setKeyword(keyword);
                
                // Parse limit parameter
                String limitStr = queryParams.getFirst("limit");
                int limit = 10; // default
                if (StringUtils.hasText(limitStr)) {
                    try {
                        limit = Integer.parseInt(limitStr);
                        if (limit <= 0) {
                            limit = 10;
                        }
                    } catch (NumberFormatException e) {
                        // Use default if parsing fails
                    }
                }
                option.setLimit(limit);
                
                // Use default highlight tags
                option.setHighlightPreTag("<B>");
                option.setHighlightPostTag("</B>");
                return option;
            })
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
