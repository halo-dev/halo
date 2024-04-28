package run.halo.app.content.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.AbstractContentService;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.Contributor;
import run.halo.app.content.ListedSinglePage;
import run.halo.app.content.ListedSnapshotDto;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.SinglePageRequest;
import run.halo.app.content.SinglePageService;
import run.halo.app.content.Stats;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Snapshot;
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
    public Flux<ListedSnapshotDto> listSnapshots(String pageName) {
        return client.fetch(SinglePage.class, pageName)
            .flatMapMany(page -> listSnapshotsBy(Ref.of(page)))
            .map(ListedSnapshotDto::from);
    }

    @Override
    public Mono<ListResult<ListedSinglePage>> list(SinglePageQuery query) {
        return client.list(SinglePage.class, query.toPredicate(),
                query.toComparator(), query.getPage(), query.getSize())
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
                        .doOnNext(username -> page.getSpec().setOwner(username))
                        .thenReturn(page);
                }
            )
            .flatMap(client::create)
            .flatMap(page -> {
                var contentRequest =
                    new ContentRequest(Ref.of(page), page.getSpec().getHeadSnapshot(),
                        null,
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
        return updateContent(baseSnapshot, pageRequest.contentRequest())
            .flatMap(contentWrapper -> {
                page.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                return client.update(page);
            });
    }

    @Override
    public Mono<SinglePage> revertToSpecifiedSnapshot(String pageName, String snapshotName) {
        return client.get(SinglePage.class, pageName)
            .filter(page -> {
                var head = page.getSpec().getHeadSnapshot();
                return !StringUtils.equals(head, snapshotName);
            })
            .flatMap(page -> {
                var baseSnapshot = page.getSpec().getBaseSnapshot();
                return getContent(snapshotName, baseSnapshot)
                    .map(content -> ContentRequest.builder()
                        .subjectRef(Ref.of(page))
                        .headSnapshotName(page.getSpec().getHeadSnapshot())
                        .content(content.getContent())
                        .raw(content.getRaw())
                        .rawType(content.getRawType())
                        .build()
                    )
                    .flatMap(contentRequest -> draftContent(baseSnapshot, contentRequest))
                    .flatMap(content -> {
                        page.getSpec().setHeadSnapshot(content.getSnapshotName());
                        return publishPageWithRetry(page);
                    });
            });
    }

    @Override
    public Mono<ContentWrapper> deleteContent(String pageName, String snapshotName) {
        return client.get(SinglePage.class, pageName)
            .flatMap(page -> {
                var headSnapshotName = page.getSpec().getHeadSnapshot();
                if (StringUtils.equals(headSnapshotName, snapshotName)) {
                    return updatePageWithRetry(page, record -> {
                        // update head to release
                        page.getSpec().setHeadSnapshot(page.getSpec().getReleaseSnapshot());
                        return record;
                    });
                }
                return Mono.just(page);
            })
            .flatMap(page -> {
                var baseSnapshotName = page.getSpec().getBaseSnapshot();
                var releaseSnapshotName = page.getSpec().getReleaseSnapshot();
                if (StringUtils.equals(releaseSnapshotName, snapshotName)) {
                    return Mono.error(new ServerWebInputException(
                        "The snapshot to delete is the release snapshot, please"
                            + " revert to another snapshot first."));
                }
                if (StringUtils.equals(baseSnapshotName, snapshotName)) {
                    return Mono.error(
                        new ServerWebInputException("The first snapshot cannot be deleted."));
                }
                return client.fetch(Snapshot.class, snapshotName)
                    .flatMap(client::delete)
                    .flatMap(deleted -> restoredContent(baseSnapshotName, deleted));
            });
    }

    private Mono<SinglePage> updatePageWithRetry(SinglePage page, UnaryOperator<SinglePage> func) {
        return client.update(func.apply(page))
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> Mono.defer(() -> client.get(SinglePage.class, page.getMetadata().getName())
                        .map(func)
                        .flatMap(client::update)
                    )
                    .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
            );
    }

    private Mono<SinglePage> publish(SinglePage singlePage) {
        var spec = singlePage.getSpec();
        spec.setPublish(true);
        if (spec.getHeadSnapshot() == null) {
            spec.setHeadSnapshot(spec.getBaseSnapshot());
        }
        spec.setReleaseSnapshot(spec.getHeadSnapshot());
        return client.update(singlePage);
    }

    Mono<SinglePage> publishPageWithRetry(SinglePage page) {
        return publish(page)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> Mono.defer(() -> client.get(SinglePage.class, page.getMetadata().getName())
                        .flatMap(this::publish))
                    .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
            );
    }

    private Mono<ListedSinglePage> getListedSinglePage(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        var listedSinglePage = new ListedSinglePage()
            .setPage(singlePage);

        var statsMono = fetchStats(singlePage)
            .doOnNext(listedSinglePage::setStats);

        var contributorsMono = listContributors(singlePage.getStatusOrDefault().getContributors())
            .collectList()
            .doOnNext(listedSinglePage::setContributors);

        var ownerMono = userService.getUserOrGhost(singlePage.getSpec().getOwner())
            .map(user -> {
                Contributor contributor = new Contributor();
                contributor.setName(user.getMetadata().getName());
                contributor.setDisplayName(user.getSpec().getDisplayName());
                contributor.setAvatar(user.getSpec().getAvatar());
                return contributor;
            })
            .doOnNext(listedSinglePage::setOwner);
        return Mono.when(statsMono, contributorsMono, ownerMono)
            .thenReturn(listedSinglePage);
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
}
