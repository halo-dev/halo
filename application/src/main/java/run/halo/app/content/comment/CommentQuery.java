package run.halo.app.content.comment;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Query criteria for comment list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CommentQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;

    public CommentQuery(ServerRequest request) {
        super(request.queryParams());
        this.exchange = request.exchange();
    }

    public String getKeyword() {
        String keyword = queryParams.getFirst("keyword");
        return StringUtils.isBlank(keyword) ? null : keyword;
    }

    public String getOwnerKind() {
        String ownerKind = queryParams.getFirst("ownerKind");
        return StringUtils.isBlank(ownerKind) ? null : ownerKind;
    }

    public String getOwnerName() {
        String ownerName = queryParams.getFirst("ownerName");
        return StringUtils.isBlank(ownerName) ? null : ownerName;
    }

    public Sort getSort() {
        var sort = SortResolver.defaultInstance.resolve(exchange);
        return sort.and(Sort.by("status.lastReplyTime",
            "spec.creationTime",
            "metadata.name"
        ).descending());
    }

    public PageRequest toPageRequest() {
        return PageRequestImpl.of(getPage(), getSize(), getSort());
    }

    /**
     * Convert to list options.
     */
    public ListOptions toListOptions() {
        var listOptions =
            labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
        var fieldQuery = listOptions.getFieldSelector().query();

        String keyword = getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            fieldQuery = and(fieldQuery, contains("spec.raw", keyword));
        }

        String ownerName = getOwnerName();
        if (StringUtils.isNotBlank(ownerName)) {
            String ownerKind = StringUtils.defaultIfBlank(getOwnerKind(), User.KIND);
            fieldQuery = and(fieldQuery,
                equal("spec.owner", Comment.CommentOwner.ownerIdentity(ownerKind, ownerName)));
        }

        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return listOptions;
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(QueryParamBuildUtil.sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Comments filtered by keyword.")
                .implementation(String.class))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("ownerKind")
                .description("Commenter kind.")
                .implementation(String.class))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("ownerName")
                .description("Commenter name.")
                .implementation(String.class));
    }
}
