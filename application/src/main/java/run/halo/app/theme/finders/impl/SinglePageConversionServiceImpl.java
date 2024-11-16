package run.halo.app.theme.finders.impl;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import static run.halo.app.core.extension.content.Post.VisibleEnum.PUBLIC;
import static run.halo.app.core.extension.content.SinglePage.PUBLISHED_LABEL;
import static run.halo.app.extension.ExtensionUtil.notDeleting;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ReactiveSinglePageContentHandler;
import run.halo.app.theme.ReactiveSinglePageContentHandler.SinglePageContentContext;
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

    private final ReactiveExtensionClient client;

    private final SinglePageService singlePageService;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    private final ExtensionGetter extensionGetter;

    @Override
    public Mono<SinglePageVo> convertToVo(SinglePage singlePage, String snapshotName) {
        return convert(singlePage, snapshotName);
    }

    @Override
    public Mono<SinglePageVo> convertToVo(@NonNull SinglePage singlePage) {
        return convert(singlePage, singlePage.getSpec().getReleaseSnapshot());
    }

    protected Mono<ContentVo> extendPageContent(SinglePage singlePage,
        ContentWrapper wrapper) {
        Assert.notNull(singlePage, "SinglePage must not be null");
        Assert.notNull(wrapper, "SinglePage content must not be null");
        return extensionGetter.getEnabledExtensions(
                ReactiveSinglePageContentHandler.class)
            .reduce(Mono.fromSupplier(() -> SinglePageContentContext.builder()
                    .singlePage(singlePage)
                    .content(wrapper.getContent())
                    .raw(wrapper.getRaw())
                    .rawType(wrapper.getRawType())
                    .build()
                ),
                (contentMono, handler) -> contentMono.flatMap(handler::handle)
            )
            .flatMap(Function.identity())
            .map(pageContent -> ContentVo.builder()
                .content(pageContent.getContent())
                .raw(pageContent.getRaw())
                .build()
            );
    }

    @Override
    public Mono<ContentVo> getContent(String pageName) {
        return client.get(SinglePage.class, pageName)
            .flatMap(singlePage -> {
                String releaseSnapshot = singlePage.getSpec().getReleaseSnapshot();
                String baseSnapshot = singlePage.getSpec().getBaseSnapshot();
                return singlePageService.getContent(releaseSnapshot, baseSnapshot)
                    .flatMap(wrapper -> extendPageContent(singlePage, wrapper));
            })
            .map(wrapper -> ContentVo.builder().content(wrapper.getContent())
                .raw(wrapper.getRaw()).build());
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

    @Override
    public Mono<ListResult<ListedSinglePageVo>> listBy(ListOptions listOptions,
        PageRequest pageRequest) {
        // rewrite list options
        var rewroteListOptions = ListOptions.builder(listOptions)
            .andQuery(notDeleting())
            .andQuery(equal("spec.deleted", Boolean.FALSE.toString()))
            .andQuery(equal("spec.visible", PUBLIC.name()))
            .labelSelector()
            .eq(PUBLISHED_LABEL, Boolean.TRUE.toString())
            .end()
            .build();

        // rewrite sort
        var rewroteSort = pageRequest.getSort()
            .and(Sort.by(
                desc("spec.pinned"),
                asc("spec.priority")
            ));

        var rewrotePageRequest =
            PageRequestImpl.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), rewroteSort);

        return client.listBy(SinglePage.class, rewroteListOptions, rewrotePageRequest)
            .flatMap(list -> Flux.fromStream(list.get())
                .flatMapSequential(this::convertToListedVo)
                .collectList()
                .map(pageVos ->
                    new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), pageVos)
                )
            );
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
            .flatMap(page -> {
                String baseSnapshot = page.getSpec().getBaseSnapshot();
                return singlePageService.getContent(snapshotName, baseSnapshot)
                    .flatMap(wrapper -> extendPageContent(singlePage, wrapper))
                    .doOnNext(page::setContent)
                    .thenReturn(page);
            })
            .flatMap(page -> contributorFinder.getContributor(page.getSpec().getOwner())
                .doOnNext(page::setOwner)
                .thenReturn(page)
            );
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
