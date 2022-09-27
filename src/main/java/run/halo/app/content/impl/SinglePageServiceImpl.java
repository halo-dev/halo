package run.halo.app.content.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.security.Principal;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentService;
import run.halo.app.content.Contributor;
import run.halo.app.content.ListedSinglePage;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.SinglePageRequest;
import run.halo.app.content.SinglePageService;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;

/**
 * Single page service implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class SinglePageServiceImpl implements SinglePageService {
    private static final Comparator<SinglePage> DEFAULT_PAGE_COMPARATOR =
        Comparator.comparing(page -> page.getMetadata().getCreationTimestamp());

    private final ContentService contentService;

    private final ReactiveExtensionClient client;

    public SinglePageServiceImpl(ContentService contentService, ReactiveExtensionClient client) {
        this.contentService = contentService;
        this.client = client;
    }

    @Override
    public Mono<ListResult<ListedSinglePage>> list(SinglePageQuery query) {
        return client.list(SinglePage.class, pageListPredicate(query),
                DEFAULT_PAGE_COMPARATOR.reversed(), query.getPage(), query.getSize())
            .flatMap(listResult -> Flux.fromStream(
                        listResult.get().map(this::getListedSinglePage)
                    )
                    .flatMap(Function.identity())
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
                    page.getSpec().setBaseSnapshot(contentWrapper.snapshotName());
                    page.getSpec().setHeadSnapshot(contentWrapper.snapshotName());
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
                page.getSpec().setHeadSnapshot(contentWrapper.snapshotName());
                return client.update(page);
            })
            .then(Mono.defer(() -> client.fetch(SinglePage.class, page.getMetadata().getName())));
    }

    @Override
    public Mono<SinglePage> publish(String name) {
        return client.fetch(SinglePage.class, name)
            .flatMap(page -> {
                SinglePage.SinglePageSpec spec = page.getSpec();
                if (Objects.equals(true, spec.getPublished())) {
                    // has been published before
                    spec.setVersion(spec.getVersion() + 1);
                } else {
                    spec.setPublished(true);
                }

                if (spec.getPublishTime() == null) {
                    spec.setPublishTime(Instant.now());
                }

                Snapshot.SubjectRef subjectRef =
                    Snapshot.SubjectRef.of(SinglePage.KIND, page.getMetadata().getName());
                return contentService.publish(spec.getHeadSnapshot(), subjectRef)
                    .flatMap(contentWrapper -> {
                        // update release snapshot name and condition
                        appendPublishedCondition(page, Post.PostPhase.PUBLISHED);
                        page.getSpec().setReleaseSnapshot(contentWrapper.snapshotName());
                        return client.update(page);
                    })
                    .then(Mono.defer(() -> client.fetch(SinglePage.class, name)));
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
            .flatMap(lsp ->
                setContributors(singlePage.getStatusOrDefault().getContributors(), lsp));
    }

    private Mono<ListedSinglePage> setContributors(List<String> usernames,
        ListedSinglePage singlePage) {
        return listContributors(usernames)
            .collectList()
            .doOnNext(singlePage::setContributors)
            .map(contributors -> singlePage)
            .defaultIfEmpty(singlePage);
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
        conditions.add(condition);

        condition.setType(phase.name());
        condition.setReason(phase.name());
        condition.setMessage("");
        condition.setStatus(ConditionStatus.TRUE);
        condition.setLastTransitionTime(Instant.now());
    }
}
