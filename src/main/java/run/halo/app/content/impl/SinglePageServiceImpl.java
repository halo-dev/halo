package run.halo.app.content.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.security.Principal;
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
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.ContentService;
import run.halo.app.content.Contributor;
import run.halo.app.content.ListedSinglePage;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.SinglePageRequest;
import run.halo.app.content.SinglePageService;
import run.halo.app.content.SinglePageSorter;
import run.halo.app.content.Stats;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.JsonUtils;
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
public class SinglePageServiceImpl implements SinglePageService {
    private final ContentService contentService;

    private final ReactiveExtensionClient client;

    private final CounterService counterService;

    public SinglePageServiceImpl(ContentService contentService, ReactiveExtensionClient client,
        CounterService counterService) {
        this.contentService = contentService;
        this.client = client;
        this.counterService = counterService;
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
        return contentService.draftContent(pageRequest.contentRequest())
            .flatMap(contentWrapper -> getContextUsername()
                .flatMap(username -> {
                    SinglePage page = pageRequest.page();
                    page.getSpec().setBaseSnapshot(contentWrapper.getSnapshotName());
                    page.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    page.getSpec().setOwner(username);
                    appendPublishedCondition(page, Post.PostPhase.DRAFT);
                    return client.create(page)
                        .then(Mono.defer(() ->
                            client.fetch(SinglePage.class,
                                pageRequest.page().getMetadata().getName())));
                }));
    }

    @Override
    public Mono<SinglePage> update(SinglePageRequest pageRequest) {
        SinglePage page = pageRequest.page();
        return contentService.updateContent(pageRequest.contentRequest())
            .flatMap(contentWrapper -> {
                page.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                return client.update(page);
            })
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            .then(Mono.defer(() -> client.fetch(SinglePage.class, page.getMetadata().getName())));
    }

    @Override
    public Mono<SinglePage> publish(String name) {
        return client.fetch(SinglePage.class, name)
            .filter(page -> Objects.equals(true, page.getSpec().getPublish()))
            .flatMap(page -> {
                final SinglePage oldPage = JsonUtils.deepCopy(page);
                final SinglePage.SinglePageSpec spec = page.getSpec();
                // if it's published state but releaseSnapshot is null, it means that need to
                // publish headSnapshot
                // if releaseSnapshot is draft and publish state is true, it means that need to
                // publish releaseSnapshot
                if (StringUtils.isBlank(spec.getHeadSnapshot())) {
                    spec.setHeadSnapshot(spec.getBaseSnapshot());
                }

                if (StringUtils.isBlank(spec.getReleaseSnapshot())) {
                    spec.setReleaseSnapshot(spec.getHeadSnapshot());
                    // first-time to publish reset version to 0
                    spec.setVersion(0);
                }
                return client.fetch(Snapshot.class, spec.getReleaseSnapshot())
                    .flatMap(releasedSnapshot -> {
                        Ref ref = Ref.of(page);
                        // not published state, need to publish
                        return contentService.publish(releasedSnapshot.getMetadata().getName(),
                                ref)
                            .flatMap(contentWrapper -> {
                                appendPublishedCondition(page, Post.PostPhase.PUBLISHED);
                                spec.setVersion(contentWrapper.getVersion());
                                SinglePage.changePublishedState(page, true);
                                if (spec.getPublishTime() == null) {
                                    spec.setPublishTime(Instant.now());
                                }
                                if (!oldPage.equals(page)) {
                                    return client.update(page);
                                }
                                return Mono.just(page);
                            });
                    })
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException(
                        String.format("Snapshot [%s] not found", spec.getReleaseSnapshot()))))
                    );
            });
    }

    private Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
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
                listedSinglePage.setStats(fetchStats(singlePage));
                return listedSinglePage;
            })
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
        return client.fetch(User.class, ownerName)
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

    Stats fetchStats(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        String name = singlePage.getMetadata().getName();
        Counter counter =
            counterService.getByName(MeterUtils.nameOf(SinglePage.class, name));
        return Stats.builder()
            .visit(counter.getVisit())
            .upvote(counter.getUpvote())
            .totalComment(counter.getTotalComment())
            .approvedComment(counter.getApprovedComment())
            .build();
    }

    private Flux<Contributor> listContributors(List<String> usernames) {
        if (usernames == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(usernames)
            .flatMap(username -> client.fetch(User.class, username))
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

    void appendPublishedCondition(SinglePage page, Post.PostPhase phase) {
        Assert.notNull(page, "The singlePage must not be null.");
        SinglePage.SinglePageStatus status = page.getStatusOrDefault();
        status.setPhase(phase.name());
        List<Condition> conditions = status.getConditionsOrDefault();
        Condition condition = new Condition();
        conditions.add(createCondition(phase));
    }

    Condition createCondition(Post.PostPhase phase) {
        Condition condition = new Condition();
        condition.setType(phase.name());
        condition.setReason(phase.name());
        condition.setMessage("");
        condition.setStatus(ConditionStatus.TRUE);
        condition.setLastTransitionTime(Instant.now());
        return condition;
    }
}
