package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;
import run.halo.app.theme.finders.vo.StatsVo;

/**
 * A default implementation of {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("singlePageFinder")
public class SinglePageFinderImpl implements SinglePageFinder {

    public static final Predicate<SinglePage> FIXED_PREDICATE = page -> page.isPublished()
        && Objects.equals(false, page.getSpec().getDeleted())
        && Post.VisibleEnum.PUBLIC.equals(page.getSpec().getVisible());

    private final ReactiveExtensionClient client;

    private final ContentService contentService;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    public SinglePageFinderImpl(ReactiveExtensionClient client, ContentService contentService,
        ContributorFinder contributorFinder, CounterService counterService) {
        this.client = client;
        this.contentService = contentService;
        this.contributorFinder = contributorFinder;
        this.counterService = counterService;
    }

    @Override
    public Mono<SinglePageVo> getByName(String pageName) {
        return client.fetch(SinglePage.class, pageName)
            .map(page -> {
                SinglePageVo pageVo = SinglePageVo.from(page);
                pageVo.setContributors(List.of());
                pageVo.setContent(ContentVo.empty());
                return pageVo;
            })
            .flatMap(singlePageVo -> fetchStats(singlePageVo)
                .doOnNext(singlePageVo::setStats)
                .thenReturn(singlePageVo)
            )
            .flatMap(this::populateContributors)
            .flatMap(page -> content(pageName)
                .doOnNext(page::setContent)
                .thenReturn(page)
            )
            .flatMap(page -> contributorFinder.getContributor(page.getSpec().getOwner())
                .doOnNext(page::setOwner)
                .thenReturn(page)
            );
    }

    @Override
    public Mono<ContentVo> content(String pageName) {
        return client.fetch(SinglePage.class, pageName)
            .map(page -> page.getSpec().getReleaseSnapshot())
            .flatMap(contentService::getContent)
            .map(wrapper -> ContentVo.builder().content(wrapper.getContent())
                .raw(wrapper.getRaw()).build());
    }

    @Override
    public Mono<ListResult<ListedSinglePageVo>> list(Integer page, Integer size) {
        return client.list(SinglePage.class, FIXED_PREDICATE,
                defaultComparator(), pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get())
                .map(singlePage -> {
                    ListedSinglePageVo pageVo = ListedSinglePageVo.from(singlePage);
                    pageVo.setContributors(List.of());
                    return pageVo;
                })
                .flatMap(lp -> fetchStats(lp).doOnNext(lp::setStats).thenReturn(lp))
                .concatMap(this::populateContributors)
                .collectList()
                .map(pageVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    pageVos)
                )
            )
            .defaultIfEmpty(new ListResult<>(0, 0, 0, List.of()));
    }

    <T extends ListedSinglePageVo> Mono<StatsVo> fetchStats(T pageVo) {
        String name = pageVo.getMetadata().getName();
        return counterService.getByName(MeterUtils.nameOf(SinglePage.class, name))
            .map(counter -> StatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(StatsVo.empty());
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

    static Comparator<SinglePage> defaultComparator() {
        Function<SinglePage, Boolean> pinned =
            page -> Objects.requireNonNullElse(page.getSpec().getPinned(), false);
        Function<SinglePage, Integer> priority =
            page -> Objects.requireNonNullElse(page.getSpec().getPriority(), 0);
        Function<SinglePage, Instant> creationTimestamp =
            page -> page.getMetadata().getCreationTimestamp();
        Function<SinglePage, String> name = page -> page.getMetadata().getName();
        return Comparator.comparing(pinned)
            .thenComparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name)
            .reversed();
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
