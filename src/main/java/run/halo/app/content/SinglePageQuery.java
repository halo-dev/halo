package run.halo.app.content;

import java.util.List;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import run.halo.app.core.extension.SinglePage;
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
    public Set<String> getContributors() {
        List<String> contributorList = queryParams.get("contributor");
        return contributorList == null ? null : Set.copyOf(contributorList);
    }
}
