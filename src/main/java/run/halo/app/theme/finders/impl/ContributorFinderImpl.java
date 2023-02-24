package run.halo.app.theme.finders.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.vo.ContributorVo;

/**
 * A default implementation of {@link ContributorFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("contributorFinder")
@RequiredArgsConstructor
public class ContributorFinderImpl implements ContributorFinder {

    private final UserService userService;

    @Override
    public Mono<ContributorVo> getContributor(String name) {
        return userService.getUserOrGhost(name)
            .map(ContributorVo::from);
    }

    @Override
    public Flux<ContributorVo> getContributors(List<String> names) {
        if (names == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(names)
            .concatMap(this::getContributor);
    }
}
