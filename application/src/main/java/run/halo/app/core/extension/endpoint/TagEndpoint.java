package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.QueryFactory.all;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.IListRequest;

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
        final var tag = "api.console.halo.run/v1alpha1/Tag";
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

    public interface ITagQuery extends IListRequest {

        @Schema(description = "Keyword for searching.")
        Optional<String> getKeyword();

        @ArraySchema(uniqueItems = true,
            arraySchema = @Schema(name = "sort",
                description = "Sort property and direction of the list result. Supported fields: "
                    + "creationTimestamp, name"),
            schema = @Schema(description = "like field,asc or field,desc",
                implementation = String.class,
                example = "creationTimestamp,desc"))
        Sort getSort();
    }

    public static class TagQuery extends IListRequest.QueryListRequest
        implements ITagQuery {

        private final ServerWebExchange exchange;

        public TagQuery(ServerRequest request) {
            super(request.queryParams());
            this.exchange = request.exchange();
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.ofNullable(queryParams.getFirst("keyword"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Sort getSort() {
            var sort = SortResolver.defaultInstance.resolve(exchange);
            sort = sort.and(Sort.by(
                Sort.Order.desc("metadata.creationTimestamp"),
                Sort.Order.asc("metadata.name")
            ));
            return sort;
        }

        public ListOptions toListOptions() {
            final var listOptions =
                labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());

            var fieldQuery = all();
            if (getKeyword().isPresent()) {
                fieldQuery = QueryFactory.and(fieldQuery, QueryFactory.or(
                    QueryFactory.contains("spec.displayName", getKeyword().get()),
                    QueryFactory.contains("spec.slug", getKeyword().get())
                ));
            }

            listOptions.setFieldSelector(listOptions.getFieldSelector().andQuery(fieldQuery));
            return listOptions;
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
