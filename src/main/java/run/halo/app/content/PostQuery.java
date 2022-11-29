package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.router.IListRequest;

/**
 * A query object for {@link Post} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PostQuery extends IListRequest.QueryListRequest {

    public PostQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
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

    @Schema(description = "Post collation.")
    public PostSorter getSort() {
        String sort = queryParams.getFirst("sort");
        return PostSorter.convertFrom(sort);
    }

    @Schema(description = "ascending order If it is true; otherwise, it is in descending order.")
    public Boolean getSortOrder() {
        String sortOrder = queryParams.getFirst("sortOrder");
        return convertBooleanOrNull(sortOrder);
    }

    @Nullable
    private Set<String> listToSet(List<String> param) {
        return param == null ? null : Set.copyOf(param);
    }

    private Boolean convertBooleanOrNull(String value) {
        return StringUtils.isBlank(value) ? null : Boolean.parseBoolean(value);
    }
}
