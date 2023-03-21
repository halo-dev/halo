package run.halo.app.content.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.AbstractContentService;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.Contributor;
import run.halo.app.content.ListedSinglePage;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.SinglePageRequest;
import run.halo.app.content.SinglePageService;
import run.halo.app.content.SinglePageSorter;
import run.halo.app.content.Stats;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;

/**
 * Single page service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Service
public class SinglePageServiceImpl extends AbstractContentService implements SinglePageService {

    private final ReactiveExtensionClient client;
    private final CounterService counterService;
    private final UserService userService;

    public SinglePageServiceImpl(ReactiveExtensionClient client, CounterService counterService,
        UserService userService) {
        super(client);
        this.client = client;
        this.counterService = counterService;
        this.userService = userService;
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(String singlePageName) {
        return client.get(SinglePage.class, singlePageName)
            .flatMap(singlePage -> {
                String headSnapshot = singlePage.getSpec().getHeadSnapshot();
                return getContent(headSnapshot, singlePage.getSpec().getBaseSnapshot());
            });
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(String singlePageName) {
        return client.get(SinglePage.class, singlePageName)
            .flatMap(singlePage -> {
                String releaseSnapshot = singlePage.getSpec().getReleaseSnapshot();
                return getContent(releaseSnapshot, singlePage.getSpec().getBaseSnapshot());
            });
    }

    @Override
    public Mono<ListResult<ListedSinglePage>> list(SinglePageQuery query) {
        Comparator<SinglePage> comparator =
            SinglePageSorter.from(query.getSort(), query.getSortOrder());
        return client.list(SinglePage.class, pageListPredicate(query),
                comparator, query.getPage(), query.getSize())
            .flatMap(listResult -> Flux.fromStream(
                        listResult.get().map(this::getListedSinglePage)
                    )
                    .concatMap(Function.identity())
                    .collectList()
                    .map(listedSinglePages -> new ListResult<>(listResult.getPage(),
                        listResult.getSize(),
                        listResult.getTotal(), listedSinglePages)
                    )
            );
    }

    @Override
    public Mono<SinglePage> draft(SinglePageRequest pageRequest) {
        return Mono.defer(
                () -> {
                    SinglePage page = pageRequest.page();
                    return getContextUsername()
                        .map(username -> {
                            page.getSpec().setOwner(username);
                            return page;
                        })
                        .defaultIfEmpty(page);
                }
            )
            .flatMap(client::create)
            .flatMap(page -> {
                var contentRequest =
                    new ContentRequest(Ref.of(page), page.getSpec().getHeadSnapshot(),
                        pageRequest.content().raw(), pageRequest.content().content(),
                        pageRequest.content().rawType());
                return draftContent(page.getSpec().getBaseSnapshot(), contentRequest)
                    .flatMap(
                        contentWrapper -> waitForPageToDraftConcludingWork(
                            page.getMetadata().getName(),
                            contentWrapper
                        )
                    );
            });
    }

    private Mono<SinglePage> waitForPageToDraftConcludingWork(String pageName,
        ContentWrapper contentWrapper) {
        return Mono.defer(() -> client.fetch(SinglePage.class, pageName)
                .flatMap(page -> {
                    page.getSpec().setBaseSnapshot(contentWrapper.getSnapshotName());
                    page.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    if (Objects.equals(true, page.getSpec().getPublish())) {
                        page.getSpec().setReleaseSnapshot(page.getSpec().getHeadSnapshot());
                    }
                    Condition condition = Condition.builder()
                        .type(Post.PostPhase.DRAFT.name())
                        .reason("DraftedSuccessfully")
                        .message("Drafted page successfully")
                        .status(ConditionStatus.TRUE)
                        .lastTransitionTime(Instant.now())
                        .build();
                    SinglePage.SinglePageStatus status = page.getStatusOrDefault();
                    status.getConditionsOrDefault().addAndEvictFIFO(condition);
                    status.setPhase(Post.PostPhase.DRAFT.name());
                    return client.update(page);
                }))
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance)
            );
    }

    @Override
    public Mono<SinglePage> update(SinglePageRequest pageRequest) {
        SinglePage page = pageRequest.page();
        String headSnapshot = page.getSpec().getHeadSnapshot();
        String releaseSnapshot = page.getSpec().getReleaseSnapshot();
        String baseSnapshot = page.getSpec().getBaseSnapshot();

        // create new snapshot to update first
        if (StringUtils.equals(headSnapshot, releaseSnapshot)) {
            return draftContent(baseSnapshot, pageRequest.contentRequest(), headSnapshot)
                .flatMap(contentWrapper -> {
                    page.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    return client.update(page);
                });
        }
        return Mono.defer(() -> updateContent(baseSnapshot, pageRequest.contentRequest())
                .flatMap(contentWrapper -> {
                    page.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    return client.update(page);
                }))
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException));
    }

    Predicate<SinglePage> pageListPredicate(SinglePageQuery query) {
        Predicate<SinglePage> paramPredicate = singlePage -> contains(query.getContributors(),
            singlePage.getStatusOrDefault().getContributors());

        String keyword = query.getKeyword();
        if (keyword != null) {
            paramPredicate = paramPredicate.and(page -> {
                String excerpt = page.getStatusOrDefault().getExcerpt();
                return StringUtils.containsIgnoreCase(excerpt, keyword)
                    || StringUtils.containsIgnoreCase(page.getSpec().getSlug(), keyword)
                    || StringUtils.containsIgnoreCase(page.getSpec().getTitle(), keyword);
            });
        }

        Post.PostPhase publishPhase = query.getPublishPhase();
        if (publishPhase != null) {
            paramPredicate = paramPredicate.and(page -> {
                if (Post.PostPhase.PENDING_APPROVAL.equals(publishPhase)) {
                    return !page.isPublished()
                        && Post.PostPhase.PENDING_APPROVAL.name()
                        .equalsIgnoreCase(page.getStatusOrDefault().getPhase());
                }
                // published
                if (Post.PostPhase.PUBLISHED.equals(publishPhase)) {
                    return page.isPublished();
                }
                // draft
                return !page.isPublished();
            });
        }

        Post.VisibleEnum visible = query.getVisible();
        if (visible != null) {
            paramPredicate =
                paramPredicate.and(post -> visible.equals(post.getSpec().getVisible()));
        }

        Predicate<SinglePage> predicate = labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector());
        return predicate.and(paramPredicate);
    }

    private Mono<ListedSinglePage> getListedSinglePage(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        return Mono.just(singlePage)
            .map(sp -> {
                ListedSinglePage listedSinglePage = new ListedSinglePage();
                listedSinglePage.setPage(singlePage);
                return listedSinglePage;
            })
            .flatMap(sp -> fetchStats(singlePage)
                .doOnNext(sp::setStats)
                .thenReturn(sp)
            )
            .flatMap(lsp ->
                setContributors(singlePage.getStatusOrDefault().getContributors(), lsp))
            .flatMap(lsp -> setOwner(singlePage.getSpec().getOwner(), lsp));
    }

    private Mono<ListedSinglePage> setContributors(List<String> usernames,
        ListedSinglePage singlePage) {
        return listContributors(usernames)
            .collectList()
            .doOnNext(singlePage::setContributors)
            .map(contributors -> singlePage)
            .defaultIfEmpty(singlePage);
    }

    private Mono<ListedSinglePage> setOwner(String ownerName, ListedSinglePage page) {
        return userService.getUserOrGhost(ownerName)
            .map(user -> {
                Contributor contributor = new Contributor();
                contributor.setName(user.getMetadata().getName());
                contributor.setDisplayName(user.getSpec().getDisplayName());
                contributor.setAvatar(user.getSpec().getAvatar());
                return contributor;
            })
            .doOnNext(page::setOwner)
            .thenReturn(page);
    }

    Mono<Stats> fetchStats(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        String name = singlePage.getMetadata().getName();
        return counterService.getByName(MeterUtils.nameOf(SinglePage.class, name))
            .map(counter -> Stats.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .totalComment(counter.getTotalComment())
                .approvedComment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(Stats.empty());
    }

    private Flux<Contributor> listContributors(List<String> usernames) {
        if (usernames == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(usernames)
            .flatMap(userService::getUserOrGhost)
            .map(user -> {
                Contributor contributor = new Contributor();
                contributor.setName(user.getMetadata().getName());
                contributor.setDisplayName(user.getSpec().getDisplayName());
                contributor.setAvatar(user.getSpec().getAvatar());
                return contributor;
            });
    }

    boolean contains(Collection<String> left, List<String> right) {
        // parameter is null, it means that ignore this condition
        if (left == null) {
            return true;
        }
        // else, it means that right is empty
        if (left.isEmpty()) {
            return right.isEmpty();
        }
        if (right == null) {
            return false;
        }
        return right.stream().anyMatch(left::contains);
    }
}
