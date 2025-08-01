package run.halo.app.theme.finders;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.theme.finders.vo.ContributorVo;

/**
 * A finder for {@link User}.
 */
public interface ContributorFinder {

    Mono<ContributorVo> getContributor(String name);

    Flux<ContributorVo> getContributors(List<String> names);
    
    /**
     * Gets the underlying UserService for batch operations.
     * This is used internally for optimization purposes.
     *
     * @return the user service instance
     */
    UserService getUserService();
}
