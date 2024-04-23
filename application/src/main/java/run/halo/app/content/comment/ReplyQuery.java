package run.halo.app.content.comment;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.router.SortableRequest;

/**
 * Query criteria for {@link Reply} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ReplyQuery extends SortableRequest {

    public ReplyQuery(ServerWebExchange exchange) {
        super(exchange);
    }

    @Schema(description = "Replies filtered by commentName.")
    public String getCommentName() {
        String commentName = queryParams.getFirst("commentName");
        if (StringUtils.isBlank(commentName)) {
            throw new ServerWebInputException("The required parameter 'commentName' is missing.");
        }
        return commentName;
    }

    /**
     * Build list options from query criteria.
     */
    public ListOptions toListOptions() {
        var listOptions =
            labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
        var newFieldSelector = listOptions.getFieldSelector()
            .andQuery(equal("spec.commentName", getCommentName()));
        listOptions.setFieldSelector(newFieldSelector);
        return listOptions;
    }

    public PageRequest toPageRequest() {
        var sort = getSort().and(Sort.by("spec.creationTime").ascending());
        return PageRequestImpl.of(getPage(), getSize(), sort);
    }

    public static void buildParameters(Builder builder) {
        SortableRequest.buildParameters(builder);
        builder.parameter(parameterBuilder()
            .in(ParameterIn.QUERY)
            .name("commentName")
            .description("Replies filtered by commentName.")
            .implementation(String.class)
            .required(true));
    }
}
