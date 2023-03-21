package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.router.IListRequest;

/**
 * Query parameter for {@link SinglePage} list.
 *
 * @author guqing
 * @since 2.0.0
 */
public class SinglePageQuery extends IListRequest.QueryListRequest {

    public SinglePageQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
    }

    @Nullable
    @Schema(name = "contributor")
    public Set<String> getContributors() {
        List<String> contributorList = queryParams.get("contributor");
        return contributorList == null ? null : Set.copyOf(contributorList);
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
    @Schema(description = "SinglePages filtered by keyword.")
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"), null);
    }

    @Schema(description = "SinglePage collation.")
    public SinglePageSorter getSort() {
        String sort = queryParams.getFirst("sort");
        return SinglePageSorter.convertFrom(sort);
    }

    @Schema(description = "ascending order If it is true; otherwise, it is in descending order.")
    public Boolean getSortOrder() {
        String sortOrder = queryParams.getFirst("sortOrder");
        return convertBooleanOrNull(sortOrder);
    }

    private Boolean convertBooleanOrNull(String value) {
        return StringUtils.isBlank(value) ? null : Boolean.parseBoolean(value);
    }
}
