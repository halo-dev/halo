package run.halo.app.theme.finders;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.theme.finders.vo.Contributor;

/**
 * A finder for {@link User}.
 */
public interface ContributorFinder {

    Mono<Contributor> getContributor(String name);

    Flux<Contributor> getContributors(List<String> names);
}
