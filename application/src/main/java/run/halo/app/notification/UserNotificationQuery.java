package run.halo.app.notification;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.or;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Notification query object for authenticated user.
 *
 * @author guqing
 * @since 2.10.0
 */
public class UserNotificationQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;

    private final String username;

    public UserNotificationQuery(ServerWebExchange exchange, String username) {
        super(exchange.getRequest().getQueryParams());
        this.exchange = exchange;
        this.username = username;
    }

    @Nullable
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    @ArraySchema(uniqueItems = true,
        arraySchema = @Schema(name = "sort",
            description = "Sort property and direction of the list result. Supported fields: "
                + "metadata.creationTimestamp"),
        schema = @Schema(description = "like field,asc or field,desc",
            implementation = String.class,
            example = "creationTimestamp,desc"))
    public Sort getSort() {
        var sort = SortResolver.defaultInstance.resolve(exchange);
        return sort.and(Sort.by(
            Sort.Order.desc("metadata.creationTimestamp"),
            Sort.Order.desc("metadata.name"))
        );
    }

    /**
     * Build a list options from the query object.
     */
    public ListOptions toListOptions() {
        var listOptions =
            labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
        var filedQuery = listOptions.getFieldSelector().query();
        if (StringUtils.isNotBlank(getKeyword())) {
            filedQuery = and(filedQuery,
                or(
                    contains("spec.title", getKeyword()),
                    contains("spec.rawContent", getKeyword())
                )
            );
        }
        if (StringUtils.isNotBlank(username)) {
            filedQuery = and(filedQuery, equal("spec.recipient", username));
        }
        listOptions.setFieldSelector(FieldSelector.of(filedQuery));
        return listOptions;
    }

    public PageRequest toPageRequest() {
        return PageRequestImpl.of(getPage(), getSize(), getSort());
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Keyword")
                .implementation(String.class)
                .required(false));
    }
}
