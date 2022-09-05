package run.halo.app.content;

import java.util.List;
import java.util.Set;
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

    private final Set<String> contributors;

    public SinglePageQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
        List<String> contributorList = queryParams.get("contributor");
        this.contributors = contributorList == null ? null : Set.copyOf(contributorList);
    }

    public Set<String> getContributors() {
        return contributors;
    }
}
