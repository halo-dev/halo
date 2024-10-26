package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.or;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;

/**
 * post tag endpoint.
 *
 * @author LIlGG
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TagEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "TagV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .GET("tags", this::listTag, builder -> {
                    builder.operationId("ListPostTags")
                        .description("List Post Tags.")
                        .tag(tag)
                        .response(
                            responseBuilder()
                                .implementation(ListResult.generateGenericClass(Tag.class))
                        );
                    TagQuery.buildParameters(builder);
                }
            )
            .build();
    }

    Mono<ServerResponse> listTag(ServerRequest request) {
        var tagQuery = new TagQuery(request);
        return client.listBy(Tag.class, tagQuery.toListOptions(),
                PageRequestImpl.of(tagQuery.getPage(), tagQuery.getSize(), tagQuery.getSort())
            )
            .flatMap(tags -> ServerResponse.ok().bodyValue(tags));
    }

    public static class TagQuery extends SortableRequest {

        public TagQuery(ServerRequest request) {
            super(request.exchange());
        }

        public Optional<String> getKeyword() {
            return Optional.ofNullable(queryParams.getFirst("keyword"))
                .filter(StringUtils::hasText);
        }

        @Override
        public ListOptions toListOptions() {
            var builder = ListOptions.builder(super.toListOptions());
            getKeyword().ifPresent(keyword -> builder.andQuery(
                or(
                    contains("spec.displayName", keyword),
                    contains("spec.slug", keyword)
                )
            ));
            return builder.build();
        }

        public static void buildParameters(Builder builder) {
            IListRequest.buildParameters(builder);
            builder.parameter(sortParameter())
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("keyword")
                    .description("Post tags filtered by keyword.")
                    .implementation(String.class)
                    .required(false));
        }
    }
}
