package run.halo.app.theme.finders.impl;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.localization.PublicLocalization;
import run.halo.app.localization.ResourceLocalizationService;
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

    private final ResourceLocalizationService localizationService;

    @Override
    public Mono<ContributorVo> getContributor(String name) {
        return userService
                .getUserOrGhost(name)
                .flatMap(user -> PublicLocalization.project(user, localizationService))
                .map(ContributorVo::from);
    }

    @Override
    public Flux<ContributorVo> getContributors(Collection<String> names) {
        return PublicLocalization.project(userService.getUsersOrGhosts(names), localizationService)
                .map(ContributorVo::from);
    }
}
