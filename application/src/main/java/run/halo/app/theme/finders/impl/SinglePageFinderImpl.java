package run.halo.app.theme.finders.impl;

import java.security.Principal;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SinglePageConversionService;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * A default implementation of {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("singlePageFinder")
@AllArgsConstructor
public class SinglePageFinderImpl implements SinglePageFinder {

    private final ReactiveExtensionClient client;

    private final SinglePageConversionService singlePagePublicQueryService;

    @Override
    public Mono<SinglePageVo> getByName(String pageName) {
        return client.get(SinglePage.class, pageName)
            .filterWhen(page -> queryPredicate().map(predicate -> predicate.test(page)))
            .flatMap(singlePagePublicQueryService::convertToVo);
    }

    @Override
    public Mono<ContentVo> content(String pageName) {
        return singlePagePublicQueryService.getContent(pageName);
    }

    @Override
    public Mono<ListResult<ListedSinglePageVo>> list(Integer page, Integer size) {
        return singlePagePublicQueryService.listBy(
            new ListOptions(),
            PageRequestImpl.of(page, size)
        );
    }

    Mono<Predicate<SinglePage>> queryPredicate() {
        Predicate<SinglePage> predicate = page -> page.isPublished()
            && Objects.equals(false, page.getSpec().getDeleted());
        Predicate<SinglePage> visiblePredicate =
            page -> Post.VisibleEnum.PUBLIC.equals(page.getSpec().getVisible());
        return currentUserName()
            .map(username -> predicate.and(
                visiblePredicate.or(page -> username.equals(page.getSpec().getOwner())))
            )
            .defaultIfEmpty(predicate.and(visiblePredicate));
    }

    Mono<String> currentUserName() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .filter(name -> !AnonymousUserConst.isAnonymousUser(name));
    }

}
