package run.halo.app.content;

import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.router.IListRequest;

/**
 * A query object for {@link Post} list.
 *
 * @author guqing
 * @since 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class PostQuery extends IListRequest.QueryListRequest {

    private final Set<String> contributors;

    private final Set<String> categories;

    private final Set<String> tags;

    public PostQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
        this.contributors = listToSet(queryParams.get("contributor"));
        this.categories = listToSet(queryParams.get("category"));
        this.tags = listToSet(queryParams.get("tag"));
    }

    public Set<String> getContributors() {
        return contributors;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public Set<String> getTags() {
        return tags;
    }

    @Nullable
    Set<String> listToSet(List<String> param) {
        return param == null ? null : Set.copyOf(param);
    }
}
