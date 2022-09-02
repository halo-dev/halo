package run.halo.app.theme.finders.impl;

import java.util.List;
import java.util.Objects;
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
    public Contributor getContributor(String name) {
        return client.fetch(User.class, name)
            .map(Contributor::from)
            .block();
    }

    @Override
    public List<Contributor> getContributors(List<String> names) {
        if (names == null) {
            return List.of();
        }
        return names.stream()
            .map(this::getContributor)
            .filter(Objects::nonNull)
            .toList();
    }
}
