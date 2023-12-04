package run.halo.app.content.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.content.AbstractContentService;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.Contributor;
import run.halo.app.content.ListedPost;
import run.halo.app.content.PostQuery;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.Stats;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
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
@Slf4j
@Component
public class PostServiceImpl extends AbstractContentService implements PostService {
    private final ReactiveExtensionClient client;
    private final CounterService counterService;
    private final UserService userService;

    public PostServiceImpl(ReactiveExtensionClient client, CounterService counterService,
        UserService userService) {
        super(client);
        this.client = client;
        this.counterService = counterService;
        this.userService = userService;
    }

    @Override
    public Mono<ListResult<ListedPost>> listPost(PostQuery query) {
        return client.list(Post.class, query.toPredicate(),
                query.toComparator(), query.getPage(), query.getSize())
            .flatMap(listResult -> Flux.fromStream(
                        listResult.get().map(this::getListedPost)
                    )
                    .concatMap(Function.identity())
                    .collectList()
                    .map(listedPosts -> new ListResult<>(listResult.getPage(), listResult.getSize(),
                        listResult.getTotal(), listedPosts)
                    )
            );
    }

    Mono<Stats> fetchStats(Post post) {
        Assert.notNull(post, "The post must not be null.");
        String name = post.getMetadata().getName();
        return counterService.getByName(MeterUtils.nameOf(Post.class, name))
            .map(counter -> Stats.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .totalComment(counter.getTotalComment())
                .approvedComment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(Stats.empty());
    }

    private Mono<ListedPost> getListedPost(Post post) {
        Assert.notNull(post, "The post must not be null.");
        return Mono.just(post)
            .map(p -> {
                ListedPost listedPost = new ListedPost();
                listedPost.setPost(p);
                return listedPost;
            })
            .flatMap(lp -> fetchStats(post)
                .doOnNext(lp::setStats)
                .thenReturn(lp)
            )
            .flatMap(lp -> setTags(post.getSpec().getTags(), lp))
            .flatMap(lp -> setCategories(post.getSpec().getCategories(), lp))
            .flatMap(lp -> setContributors(post.getStatusOrDefault().getContributors(), lp))
            .flatMap(lp -> setOwner(post.getSpec().getOwner(), lp));
    }

    private Mono<ListedPost> setTags(List<String> tagNames, ListedPost post) {
        return listTags(tagNames)
            .collectList()
            .doOnNext(post::setTags)
            .map(tags -> post)
            .switchIfEmpty(Mono.defer(() -> Mono.just(post)));
    }

    private Mono<ListedPost> setCategories(List<String> categoryNames, ListedPost post) {
        return listCategories(categoryNames)
            .collectList()
            .doOnNext(post::setCategories)
            .map(categories -> post)
            .switchIfEmpty(Mono.defer(() -> Mono.just(post)));
    }

    private Mono<ListedPost> setContributors(List<String> contributorNames, ListedPost post) {
        return listContributors(contributorNames)
            .collectList()
            .doOnNext(post::setContributors)
            .map(contributors -> post)
            .switchIfEmpty(Mono.defer(() -> Mono.just(post)));
    }

    private Mono<ListedPost> setOwner(String ownerName, ListedPost post) {
        return userService.getUserOrGhost(ownerName)
            .map(user -> {
                Contributor contributor = new Contributor();
                contributor.setName(user.getMetadata().getName());
                contributor.setDisplayName(user.getSpec().getDisplayName());
                contributor.setAvatar(user.getSpec().getAvatar());
                return contributor;
            })
            .doOnNext(post::setOwner)
            .thenReturn(post);
    }

    private Flux<Tag> listTags(List<String> tagNames) {
        if (tagNames == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(tagNames)
            .flatMapSequential(tagName -> client.fetch(Tag.class, tagName));
    }

    private Flux<Category> listCategories(List<String> categoryNames) {
        if (categoryNames == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(categoryNames)
            .flatMapSequential(categoryName -> client.fetch(Category.class, categoryName));
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

    @Override
    public Mono<Post> draftPost(PostRequest postRequest) {
        return Mono.defer(
                () -> {
                    Post post = postRequest.post();
                    return getContextUsername()
                        .map(username -> {
                            post.getSpec().setOwner(username);
                            return post;
                        })
                        .defaultIfEmpty(post);
                }
            )
            .flatMap(client::create)
            .flatMap(post -> {
                if (postRequest.content() == null) {
                    return Mono.just(post);
                }
                var contentRequest =
                    new ContentRequest(Ref.of(post), post.getSpec().getHeadSnapshot(),
                        postRequest.content().raw(), postRequest.content().content(),
                        postRequest.content().rawType());
                return draftContent(post.getSpec().getBaseSnapshot(), contentRequest)
                    .flatMap(contentWrapper -> waitForPostToDraftConcludingWork(
                        post.getMetadata().getName(),
                        contentWrapper)
                    );
            })
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<Post> waitForPostToDraftConcludingWork(String postName,
        ContentWrapper contentWrapper) {
        return Mono.defer(() -> client.fetch(Post.class, postName)
                .flatMap(post -> {
                    post.getSpec().setBaseSnapshot(contentWrapper.getSnapshotName());
                    post.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    if (Objects.equals(true, post.getSpec().getPublish())) {
                        post.getSpec().setReleaseSnapshot(post.getSpec().getHeadSnapshot());
                    }
                    Condition condition = Condition.builder()
                        .type(Post.PostPhase.DRAFT.name())
                        .reason("DraftedSuccessfully")
                        .message("Drafted post successfully.")
                        .status(ConditionStatus.TRUE)
                        .lastTransitionTime(Instant.now())
                        .build();
                    Post.PostStatus status = post.getStatusOrDefault();
                    status.setPhase(Post.PostPhase.DRAFT.name());
                    status.getConditionsOrDefault().addAndEvictFIFO(condition);
                    return client.update(post);
                }))
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    @Override
    public Mono<Post> updatePost(PostRequest postRequest) {
        Post post = postRequest.post();
        String headSnapshot = post.getSpec().getHeadSnapshot();
        String releaseSnapshot = post.getSpec().getReleaseSnapshot();
        String baseSnapshot = post.getSpec().getBaseSnapshot();

        if (StringUtils.equals(releaseSnapshot, headSnapshot)) {
            // create new snapshot to update first
            return draftContent(baseSnapshot, postRequest.contentRequest(), headSnapshot)
                .flatMap(contentWrapper -> {
                    post.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    return client.update(post);
                });
        }
        return Mono.defer(() -> updateContent(baseSnapshot, postRequest.contentRequest())
                .flatMap(contentWrapper -> {
                    post.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
                    return client.update(post);
                }))
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(throwable -> throwable instanceof OptimisticLockingFailureException));
    }

    @Override
    public Mono<Post> updateBy(@NonNull Post post) {
        return client.update(post);
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(String postName) {
        return client.get(Post.class, postName)
            .flatMap(this::getHeadContent);
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(Post post) {
        var headSnapshot = post.getSpec().getHeadSnapshot();
        return getContent(headSnapshot, post.getSpec().getBaseSnapshot());
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(String postName) {
        return client.get(Post.class, postName)
            .flatMap(this::getReleaseContent);
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(Post post) {
        var releaseSnapshot = post.getSpec().getReleaseSnapshot();
        return getContent(releaseSnapshot, post.getSpec().getBaseSnapshot());
    }

    @Override
    public Mono<Post> publish(Post post) {
        return Mono.just(post)
            .doOnNext(p -> {
                var spec = post.getSpec();
                spec.setPublish(true);
                if (spec.getHeadSnapshot() == null) {
                    spec.setHeadSnapshot(spec.getBaseSnapshot());
                }
                spec.setReleaseSnapshot(spec.getHeadSnapshot());
            }).flatMap(client::update);
    }

    @Override
    public Mono<Post> unpublish(Post post) {
        return Mono.just(post)
            .doOnNext(p -> p.getSpec().setPublish(false))
            .flatMap(client::update);
    }

    @Override
    public Mono<Post> getByUsername(String postName, String username) {
        return client.get(Post.class, postName)
            .filter(post -> post.getSpec() != null)
            .filter(post -> Objects.equals(username, post.getSpec().getOwner()));
    }
}
