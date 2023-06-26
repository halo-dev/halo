package run.halo.app.theme.finders.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.SinglePageConversionService;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;
import run.halo.app.theme.finders.vo.StatsVo;

/**
 * Default implementation of {@link SinglePageConversionService}.
 *
 * @author guqing
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class SinglePageConversionServiceImpl implements SinglePageConversionService {

    private final SinglePageService singlePageService;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    @Override
    public Mono<SinglePageVo> convertToVo(SinglePage singlePage, String snapshotName) {
        return convert(singlePage, snapshotName);
    }

    @Override
    public Mono<SinglePageVo> convertToVo(SinglePage singlePage) {
        return convert(singlePage, singlePage.getSpec().getReleaseSnapshot());
    }

    @Override
    public Mono<ListedSinglePageVo> convertToListedVo(SinglePage singlePage) {
        return Mono.fromSupplier(
                () -> {
                    ListedSinglePageVo pageVo = ListedSinglePageVo.from(singlePage);
                    pageVo.setContributors(List.of());
                    return pageVo;
                })
            .flatMap(this::populateStats)
            .flatMap(this::populateContributors);
    }

    Mono<SinglePageVo> convert(SinglePage singlePage, String snapshotName) {
        Assert.notNull(singlePage, "Single page must not be null");
        Assert.hasText(snapshotName, "Snapshot name must not be empty");
        return Mono.just(singlePage)
            .map(page -> {
                SinglePageVo pageVo = SinglePageVo.from(page);
                pageVo.setContributors(List.of());
                pageVo.setContent(ContentVo.empty());
                return pageVo;
            })
            .flatMap(this::populateStats)
            .flatMap(this::populateContributors)
            .flatMap(page -> populateContent(page, snapshotName))
            .flatMap(page -> contributorFinder.getContributor(page.getSpec().getOwner())
                .doOnNext(page::setOwner)
                .thenReturn(page)
            );
    }

    Mono<SinglePageVo> populateContent(SinglePageVo singlePageVo, String snapshotName) {
        Assert.notNull(singlePageVo, "Single page vo must not be null");
        Assert.hasText(snapshotName, "Snapshot name must not be empty");
        return singlePageService.getContent(snapshotName, singlePageVo.getSpec().getBaseSnapshot())
            .map(contentWrapper -> ContentVo.builder()
                .content(contentWrapper.getContent())
                .raw(contentWrapper.getRaw())
                .build()
            )
            .doOnNext(singlePageVo::setContent)
            .thenReturn(singlePageVo);
    }

    <T extends ListedSinglePageVo> Mono<T> populateStats(T pageVo) {
        String name = pageVo.getMetadata().getName();
        return counterService.getByName(MeterUtils.nameOf(SinglePage.class, name))
            .map(counter -> StatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            )
            .doOnNext(pageVo::setStats)
            .thenReturn(pageVo);
    }

    <T extends ListedSinglePageVo> Mono<T> populateContributors(T pageVo) {
        List<String> names = pageVo.getStatus().getContributors();
        if (CollectionUtils.isEmpty(names)) {
            return Mono.just(pageVo);
        }
        return contributorFinder.getContributors(names)
            .collectList()
            .doOnNext(pageVo::setContributors)
            .thenReturn(pageVo);
    }
}
