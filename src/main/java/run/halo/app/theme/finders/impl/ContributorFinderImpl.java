package run.halo.app.theme.finders.impl;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.vo.Contributor;

/**
 * A default implementation of {@link ContributorFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("contributorFinder")
public class ContributorFinderImpl implements ContributorFinder {

    private final ReactiveExtensionClient client;

    public ContributorFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Contributor> getContributor(String name) {
        return client.fetch(User.class, name)
            .map(Contributor::from);
    }

    @Override
    public Flux<Contributor> getContributors(List<String> names) {
        if (names == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(names)
            .concatMap(this::getContributor);
    }
}
