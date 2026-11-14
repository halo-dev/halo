package run.halo.app.content.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.*;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;

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

    public SinglePageServiceImpl(
            ReactiveExtensionClient client, CounterService counterService, UserService userService) {
        super(client);
        this.client = client;
        this.counterService = counterService;
        this.userService = userService;
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(String singlePageName) {
        return client.get(SinglePage.class, singlePageName).flatMap(singlePage -> {
            String headSnapshot = singlePage.getSpec().getHeadSnapshot();
            return getContent(headSnapshot, singlePage.getSpec().getBaseSnapshot());
        });
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(String singlePageName) {
        return client.get(SinglePage.class, singlePageName).flatMap(singlePage -> {
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
        return client.listBy(SinglePage.class, query.toListOptions(), query.toPageRequest())
                .flatMap(listResult -> assembleListedSinglePages(
                                listResult.get().toList())
                        .map(listedSinglePages -> new ListResult<>(
                                listResult.getPage(), listResult.getSize(), listResult.getTotal(), listedSinglePages)));
    }

    @Override
    public Mono<SinglePage> draft(SinglePageRequest pageRequest) {
        return Mono.defer(() -> {
                    SinglePage page = pageRequest.page();
                    return getContextUsername()
                            .doOnNext(username -> page.getSpec().setOwner(username))
                            .thenReturn(page);
                })
                .flatMap(client::create)
                .flatMap(page -> {
                    var contentRequest = new ContentRequest(
                            Ref.of(page),
                            page.getSpec().getHeadSnapshot(),
                            null,
                            pageRequest.content().raw(),
                            pageRequest.content().content(),
                            pageRequest.content().rawType());
                    return draftContent(page.getSpec().getBaseSnapshot(), contentRequest)
                            .flatMap(contentWrapper -> waitForPageToDraftConcludingWork(
                                    page.getMetadata().getName(), contentWrapper));
                });
    }

    private Mono<SinglePage> waitForPageToDraftConcludingWork(String pageName, ContentWrapper contentWrapper) {
        return Mono.defer(() -> client.fetch(SinglePage.class, pageName).flatMap(page -> {
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
                        .filter(OptimisticLockingFailureException.class::isInstance));
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
        return updateContent(baseSnapshot, pageRequest.contentRequest()).flatMap(contentWrapper -> {
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
                                    .build())
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
                        return Mono.error(new ServerWebInputException("problemDetail.content.snapshot.published"));
                    }
                    if (StringUtils.equals(baseSnapshotName, snapshotName)) {
                        return Mono.error(
                                new UnsatisfiedAttributeValueException("problemDetail.content.snapshot.first"));
                    }
                    return client.fetch(Snapshot.class, snapshotName)
                            .flatMap(client::delete)
                            .flatMap(deleted -> restoredContent(baseSnapshotName, deleted));
                });
    }

    private Mono<SinglePage> updatePageWithRetry(SinglePage page, UnaryOperator<SinglePage> func) {
        return client.update(func.apply(page))
                .onErrorResume(
                        OptimisticLockingFailureException.class,
                        e -> Mono.defer(() -> client.get(
                                                SinglePage.class,
                                                page.getMetadata().getName())
                                        .map(func)
                                        .flatMap(client::update))
                                .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                                        .filter(OptimisticLockingFailureException.class::isInstance)));
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
                .onErrorResume(
                        OptimisticLockingFailureException.class,
                        e -> Mono.defer(() -> client.get(
                                                SinglePage.class,
                                                page.getMetadata().getName())
                                        .flatMap(this::publish))
                                .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                                        .filter(OptimisticLockingFailureException.class::isInstance)));
    }

    private Mono<List<ListedSinglePage>> assembleListedSinglePages(List<SinglePage> singlePages) {
        if (singlePages.isEmpty()) {
            return Mono.just(List.of());
        }
        var usernames = new LinkedHashSet<String>();
        usernames.add(UserService.GHOST_USER_NAME);
        singlePages.forEach(singlePage -> {
            var owner = singlePage.getSpec().getOwner();
            if (StringUtils.isNotBlank(owner)) {
                usernames.add(owner);
            }
            var contributors = singlePage.getStatusOrDefault().getContributors();
            if (contributors != null) {
                contributors.stream().filter(StringUtils::isNotBlank).forEach(usernames::add);
            }
        });

        var statsMono = fetchStatsByPageName(singlePages);
        var usersMono = fetchUsersByName(List.copyOf(usernames));

        return Mono.zip(statsMono, usersMono).map(tuple -> {
            var statsByCounterName = tuple.getT1();
            var usersByName = tuple.getT2();
            return singlePages.stream()
                    .map(singlePage -> toListedSinglePage(singlePage, statsByCounterName, usersByName))
                    .toList();
        });
    }

    private Mono<Map<String, Stats>> fetchStatsByPageName(List<SinglePage> singlePages) {
        var counterNames = singlePages.stream()
                .map(singlePage -> MeterUtils.nameOf(
                        SinglePage.class, singlePage.getMetadata().getName()))
                .toList();
        return counterService
                .getByNames(counterNames)
                .collectMap(counter -> counter.getMetadata().getName(), SinglePageServiceImpl::toStats);
    }

    private Mono<Map<String, Contributor>> fetchUsersByName(List<String> usernames) {
        return userService.getUsersOrGhosts(usernames).collectList().map(users -> {
            var usersByName = new HashMap<String, Contributor>();
            users.forEach(user -> usersByName.putIfAbsent(user.getMetadata().getName(), toContributor(user)));
            return usersByName;
        });
    }

    private static ListedSinglePage toListedSinglePage(
            SinglePage singlePage, Map<String, Stats> statsByCounterName, Map<String, Contributor> usersByName) {
        var listedSinglePage = new ListedSinglePage().setPage(singlePage);

        var counterName =
                MeterUtils.nameOf(SinglePage.class, singlePage.getMetadata().getName());
        listedSinglePage.setStats(statsByCounterName.getOrDefault(counterName, Stats.empty()));

        var ghost = usersByName.get(UserService.GHOST_USER_NAME);
        listedSinglePage.setOwner(
                resolveUser(usersByName, ghost, singlePage.getSpec().getOwner()));

        var contributorNames = singlePage.getStatusOrDefault().getContributors();
        if (CollectionUtils.isEmpty(contributorNames)) {
            listedSinglePage.setContributors(List.of());
        } else {
            listedSinglePage.setContributors(contributorNames.stream()
                    .map(name -> resolveUser(usersByName, ghost, name))
                    .filter(Objects::nonNull)
                    .toList());
        }
        return listedSinglePage;
    }

    private static Contributor resolveUser(Map<String, Contributor> usersByName, Contributor ghost, String username) {
        if (StringUtils.isBlank(username)) {
            return ghost;
        }
        return usersByName.getOrDefault(username, ghost);
    }

    private static Stats toStats(Counter counter) {
        return Stats.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .totalComment(counter.getTotalComment())
                .approvedComment(counter.getApprovedComment())
                .build();
    }

    private static Contributor toContributor(User user) {
        var contributor = new Contributor();
        contributor.setName(user.getMetadata().getName());
        contributor.setDisplayName(user.getSpec().getDisplayName());
        contributor.setAvatar(user.getSpec().getAvatar());
        return contributor;
    }
}
