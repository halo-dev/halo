package run.halo.app.content;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;

/**
 * A query object for {@link Post} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PostQuery extends IListRequest.QueryListRequest {

    private final ServerWebExchange exchange;

    private final String username;

    public PostQuery(ServerRequest request) {
        this(request, null);
    }

    public PostQuery(ServerRequest request, @Nullable String username) {
        super(request.queryParams());
        this.exchange = request.exchange();
        this.username = username;
    }

    @Schema(hidden = true)
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @Nullable
    @Schema(name = "contributor")
    public Set<String> getContributors() {
        return listToSet(queryParams.get("contributor"));
    }

    @Nullable
    @Schema(name = "category")
    public Set<String> getCategories() {
        return listToSet(queryParams.get("category"));
    }

    @Nullable
    @Schema(name = "tag")
    public Set<String> getTags() {
        return listToSet(queryParams.get("tag"));
    }

    @Nullable
    public Post.PostPhase getPublishPhase() {
        String publishPhase = queryParams.getFirst("publishPhase");
        return Post.PostPhase.from(publishPhase);
    }

    @Nullable
    public Post.VisibleEnum getVisible() {
        String visible = queryParams.getFirst("visible");
        return Post.VisibleEnum.from(visible);
    }

    @Nullable
    @Schema(description = "Posts filtered by keyword.")
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    @ArraySchema(uniqueItems = true,
        arraySchema = @Schema(name = "sort",
            description = "Sort property and direction of the list result. Supported fields: "
                + "creationTimestamp,publishTime"),
        schema = @Schema(description = "like field,asc or field,desc",
            implementation = String.class,
            example = "creationTimestamp,desc"))
    public Sort getSort() {
        return SortResolver.defaultInstance.resolve(exchange);
    }

    @Nullable
    private Set<String> listToSet(List<String> param) {
        return param == null ? null : Set.copyOf(param);
    }

    /**
     * Build a list options from the query object.
     *
     * @return a list options
     */
    public ListOptions toListOptions() {
        var listOptions =
            labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
        if (listOptions.getFieldSelector() == null) {
            listOptions.setFieldSelector(FieldSelector.all());
        }
        var labelSelectorBuilder = LabelSelector.builder();
        var fieldQuery = QueryFactory.all();

        String keyword = getKeyword();
        if (keyword != null) {
            fieldQuery = QueryFactory.and(fieldQuery, QueryFactory.or(
                QueryFactory.contains("status.excerpt", keyword),
                QueryFactory.contains("spec.slug", keyword),
                QueryFactory.contains("spec.title", keyword)
            ));
        }

        Post.PostPhase publishPhase = getPublishPhase();
        if (publishPhase != null) {
            if (Post.PostPhase.PENDING_APPROVAL.equals(publishPhase)) {
                fieldQuery = QueryFactory.and(fieldQuery, QueryFactory.equal(
                    "status.phase", Post.PostPhase.PENDING_APPROVAL.name())
                );
                labelSelectorBuilder.eq(Post.PUBLISHED_LABEL, BooleanUtils.FALSE);
            } else if (Post.PostPhase.PUBLISHED.equals(publishPhase)) {
                labelSelectorBuilder.eq(Post.PUBLISHED_LABEL, BooleanUtils.TRUE);
            } else {
                labelSelectorBuilder.eq(Post.PUBLISHED_LABEL, BooleanUtils.FALSE);
            }
        }

        Post.VisibleEnum visible = getVisible();
        if (visible != null) {
            fieldQuery = QueryFactory.and(fieldQuery, QueryFactory.equal(
                "spec.visible", visible.name())
            );
        }

        if (StringUtils.isNotBlank(username)) {
            fieldQuery = QueryFactory.and(fieldQuery, QueryFactory.equal(
                "spec.owner", username)
            );
        }

        listOptions.setFieldSelector(listOptions.getFieldSelector().andQuery(fieldQuery));
        listOptions.setLabelSelector(
            listOptions.getLabelSelector().and(labelSelectorBuilder.build()));
        return listOptions;
    }
}
