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
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.ContentService;
import run.halo.app.content.Contributor;
import run.halo.app.content.ListedPost;
import run.halo.app.content.PostQuery;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.PostSorter;
import run.halo.app.content.Stats;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.core.extension.Tag;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;

/**
 * A default implementation of {@link PostService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostServiceImpl implements PostService {
    private final ContentService contentService;
    private final ReactiveExtensionClient client;
    private final CounterService counterService;

    public PostServiceImpl(ContentService contentService, ReactiveExtensionClient client,
        CounterService counterService) {
        this.contentService = contentService;
        this.client = client;
        this.counterService = counterService;
    }

    @Override
    public Mono<ListResult<ListedPost>> listPost(PostQuery query) {
        Comparator<Post> comparator =
            PostSorter.from(query.getSort(), query.getSortOrder());
        return client.list(Post.class, postListPredicate(query),
                comparator, query.getPage(), query.getSize())
            .flatMap(listResult -> Flux.fromStream(
                        listResult.get().map(this::getListedPost)
                    )
                    .flatMap(Function.identity())
                    .collectList()
                    .map(listedPosts -> new ListResult<>(listResult.getPage(), listResult.getSize(),
                        listResult.getTotal(), listedPosts)
                    )
            );
    }

    Stats fetchStats(Post post) {
        Assert.notNull(post, "The post must not be null.");
        String name = post.getMetadata().getName();
        Counter counter =
            counterService.getByName(MeterUtils.nameOf(Post.class, name));
        return Stats.builder()
            .visit(counter.getVisit())
            .upvote(counter.getUpvote())
            .totalComment(counter.getTotalComment())
            .approvedComment(counter.getApprovedComment())
            .build();
    }

    Predicate<Post> postListPredicate(PostQuery query) {
        Predicate<Post> paramPredicate = post ->
            contains(query.getCategories(), post.getSpec().getCategories())
                && contains(query.getTags(), post.getSpec().getTags())
                && contains(query.getContributors(), post.getStatus().getContributors());

        String keyword = query.getKeyword();
        if (keyword != null) {
            paramPredicate = paramPredicate.and(post -> {
                String excerpt = post.getStatusOrDefault().getExcerpt();
                return StringUtils.containsIgnoreCase(excerpt, keyword)
                    || StringUtils.containsIgnoreCase(post.getSpec().getSlug(), keyword)
                    || StringUtils.containsIgnoreCase(post.getSpec().getTitle(), keyword);
            });
        }

        Post.PostPhase publishPhase = query.getPublishPhase();
        if (publishPhase != null) {
            paramPredicate = paramPredicate.and(post -> {
                if (Post.PostPhase.PENDING_APPROVAL.equals(publishPhase)) {
                    return !post.isPublished()
                        && Post.PostPhase.PENDING_APPROVAL.name()
                        .equalsIgnoreCase(post.getStatusOrDefault().getPhase());
                }
                // published
                if (Post.PostPhase.PUBLISHED.equals(publishPhase)) {
                    return post.isPublished();
                }
                // draft
                return !post.isPublished();
            });
        }

        Post.VisibleEnum visible = query.getVisible();
        if (visible != null) {
            paramPredicate =
                paramPredicate.and(post -> visible.equals(post.getSpec().getVisible()));
        }

        Predicate<Post> predicate = labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector());
        return predicate.and(paramPredicate);
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

    private Mono<ListedPost> getListedPost(Post post) {
        Assert.notNull(post, "The post must not be null.");
        return Mono.just(post)
            .map(p -> {
                ListedPost listedPost = new ListedPost();
                listedPost.setPost(p);
                listedPost.setStats(fetchStats(post));
                return listedPost;
            })
            .flatMap(lp -> setTags(post.getSpec().getTags(), lp))
            .flatMap(lp -> setCategories(post.getSpec().getCategories(), lp))
            .flatMap(lp -> setContributors(post.getStatus().getContributors(), lp));
    }

    private Mono<ListedPost> setTags(List<String> tagNames, ListedPost post) {
        return listTags(tagNames)
            .collectSortedList()
            .doOnNext(post::setTags)
            .map(tags -> post)
            .switchIfEmpty(Mono.defer(() -> Mono.just(post)));
    }

    private Mono<ListedPost> setCategories(List<String> categoryNames, ListedPost post) {
        return listCategories(categoryNames)
            .collectSortedList()
            .doOnNext(post::setCategories)
            .map(categories -> post)
            .switchIfEmpty(Mono.defer(() -> Mono.just(post)));
    }

    private Mono<ListedPost> setContributors(List<String> contributorNames, ListedPost post) {
        return listContributors(contributorNames)
            .collectSortedList()
            .doOnNext(post::setContributors)
            .map(contributors -> post)
            .switchIfEmpty(Mono.defer(() -> Mono.just(post)));
    }

    private Flux<Tag> listTags(List<String> tagNames) {
        if (tagNames == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(tagNames)
            .flatMap(tagName -> client.fetch(Tag.class, tagName));
    }

    private Flux<Category> listCategories(List<String> categoryNames) {
        if (categoryNames == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(categoryNames)
            .flatMap(categoryName -> client.fetch(Category.class, categoryName));
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

    @Override
    public Mono<Post> draftPost(PostRequest postRequest) {
        return contentService.draftContent(postRequest.contentRequest())
            .flatMap(contentWrapper -> getContextUsername()
                .flatMap(username -> {
                    Post post = postRequest.post();
                    post.getSpec().setBaseSnapshot(contentWrapper.snapshotName());
                    post.getSpec().setHeadSnapshot(contentWrapper.snapshotName());
                    post.getSpec().setOwner(username);
                    appendPublishedCondition(post, Post.PostPhase.DRAFT);
                    return client.create(post)
                        .then(Mono.defer(() ->
                            client.fetch(Post.class, postRequest.post().getMetadata().getName())));
                }));
    }

    @Override
    public Mono<Post> updatePost(PostRequest postRequest) {
        Post post = postRequest.post();
        return contentService.updateContent(postRequest.contentRequest())
            .flatMap(contentWrapper -> {
                post.getSpec().setHeadSnapshot(contentWrapper.snapshotName());
                return client.update(post);
            })
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
            .then(Mono.defer(() -> client.fetch(Post.class, post.getMetadata().getName())));
    }

    private Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName);
    }

    @Override
    public Mono<Post> publishPost(String postName) {
        return client.fetch(Post.class, postName)
            .flatMap(post -> {
                Post.PostSpec spec = post.getSpec();
                // publish snapshot
                return Mono.zip(Mono.just(post),
                    client.fetch(Snapshot.class, spec.getHeadSnapshot()));
            })
            .flatMap(tuple -> {
                Post post = tuple.getT1();
                Snapshot snapshot = tuple.getT2();

                Post.PostSpec postSpec = post.getSpec();
                if (Objects.equals(true, postSpec.getPublished())) {
                    // has been published before
                    postSpec.setVersion(postSpec.getVersion() + 1);
                } else {
                    postSpec.setPublished(true);
                }

                if (postSpec.getPublishTime() == null) {
                    postSpec.setPublishTime(Instant.now());
                }

                // update release snapshot name and condition
                postSpec.setReleaseSnapshot(snapshot.getMetadata().getName());
                appendPublishedCondition(post, Post.PostPhase.PUBLISHED);

                Snapshot.SubjectRef subjectRef =
                    Snapshot.SubjectRef.of(Post.KIND, post.getMetadata().getName());
                return contentService.publish(snapshot.getMetadata().getName(), subjectRef)
                    .flatMap(contentWrapper -> {
                        post.getSpec().setReleaseSnapshot(contentWrapper.snapshotName());
                        return client.update(post);
                    })
                    .then(Mono.defer(() -> client.fetch(Post.class, postName)));
            })
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException));
    }

    void appendPublishedCondition(Post post, Post.PostPhase phase) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostStatus status = post.getStatusOrDefault();
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
