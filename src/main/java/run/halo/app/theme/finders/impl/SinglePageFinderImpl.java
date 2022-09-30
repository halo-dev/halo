package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.Contributor;
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

    public static final Predicate<SinglePage> FIXED_PREDICATE = page ->
        Objects.equals(false, page.getSpec().getDeleted())
            && Objects.equals(true, page.getSpec().getPublished())
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
    public SinglePageVo getByName(String pageName) {
        SinglePage page = client.fetch(SinglePage.class, pageName)
            .block();
        if (page == null) {
            return null;
        }
        List<Contributor> contributors =
            contributorFinder.getContributors(page.getStatus().getContributors());
        SinglePageVo pageVo = SinglePageVo.from(page);
        pageVo.setContributors(contributors);
        pageVo.setContent(content(pageName));
        populateStats(pageVo);
        return pageVo;
    }

    @Override
    public ContentVo content(String pageName) {
        return client.fetch(SinglePage.class, pageName)
            .map(page -> page.getSpec().getReleaseSnapshot())
            .flatMap(contentService::getContent)
            .map(wrapper -> ContentVo.builder().content(wrapper.content())
                .raw(wrapper.raw()).build())
            .block();
    }

    @Override
    public ListResult<SinglePageVo> list(Integer page, Integer size) {
        ListResult<SinglePage> list = client.list(SinglePage.class, FIXED_PREDICATE,
                defaultComparator(), pageNullSafe(page), sizeNullSafe(size))
            .block();
        if (list == null) {
            return new ListResult<>(0, 0, 0, List.of());
        }
        List<SinglePageVo> pageVos = list.get()
            .map(sp -> {
                List<Contributor> contributors =
                    contributorFinder.getContributors(sp.getStatus().getContributors());
                SinglePageVo pageVo = SinglePageVo.from(sp);
                pageVo.setContributors(contributors);
                populateStats(pageVo);
                return pageVo;
            })
            .toList();
        return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), pageVos);
    }

    void populateStats(SinglePageVo pageVo) {
        String name = pageVo.getMetadata().getName();
        Counter counter =
            counterService.getByName(MeterUtils.nameOf(SinglePage.class, name));
        StatsVo statsVo = StatsVo.builder()
            .visit(counter.getVisit())
            .upvote(counter.getUpvote())
            .comment(counter.getApprovedComment())
            .build();
        pageVo.setStats(statsVo);
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
