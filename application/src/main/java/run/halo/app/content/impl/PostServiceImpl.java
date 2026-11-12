package run.halo.app.content.impl;

import static run.halo.app.extension.index.query.Queries.in;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
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
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.*;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;

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
    private final CategoryService categoryService;

    public PostServiceImpl(
            ReactiveExtensionClient client,
            CounterService counterService,
            UserService userService,
            CategoryService categoryService) {
        super(client);
        this.client = client;
        this.counterService = counterService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @Override
    public Mono<ListResult<ListedPost>> listPost(PostQuery query) {
        return buildListOptions(query)
                .flatMap(listOptions -> client.listBy(Post.class, listOptions, query.toPageRequest()))
                .flatMap(listResult -> assembleListedPosts(listResult.get().toList())
                        .map(listedPosts -> new ListResult<>(
                                listResult.getPage(), listResult.getSize(), listResult.getTotal(), listedPosts)))
                .defaultIfEmpty(ListResult.emptyResult());
    }

    Mono<ListOptions> buildListOptions(PostQuery query) {
        var categoryName = query.getCategoryWithChildren();
        if (categoryName == null) {
            return Mono.just(query.toListOptions());
        }
        return categoryService.listChildren(categoryName).collectList().map(categories -> {
            var categoryNames = categories.stream()
                    .map(Category::getMetadata)
                    .map(MetadataOperator::getName)
                    .toList();
            var listOptions = query.toListOptions();
            var newFiledSelector = listOptions.getFieldSelector().andQuery(in("spec.categories", categoryNames));
            listOptions.setFieldSelector(newFiledSelector);
            return listOptions;
        });
    }

    private Mono<List<ListedPost>> assembleListedPosts(List<Post> posts) {
        if (posts.isEmpty()) {
            return Mono.just(List.of());
        }
        var tagNames = posts.stream()
                .map(post -> post.getSpec().getTags())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .toList();
        var categoryNames = posts.stream()
                .map(post -> post.getSpec().getCategories())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .toList();
        var usernames = new LinkedHashSet<String>();
        usernames.add(UserService.GHOST_USER_NAME);
        posts.forEach(post -> {
            var owner = post.getSpec().getOwner();
            if (StringUtils.isNotBlank(owner)) {
                usernames.add(owner);
            }
            var contributors = post.getStatusOrDefault().getContributors();
            if (contributors != null) {
                contributors.stream().filter(StringUtils::isNotBlank).forEach(usernames::add);
            }
        });

        var statsMono = fetchStatsByPostName(posts);
        var tagsMono = listTags(tagNames).collectList();
        var categoriesMono = listCategories(categoryNames).collectList();
        var usersMono = fetchUsersByName(List.copyOf(usernames));

        return Mono.zip(statsMono, tagsMono, categoriesMono, usersMono).map(tuple -> {
            var statsByCounterName = tuple.getT1();
            var tags = tuple.getT2();
            var categories = tuple.getT3();
            var usersByName = tuple.getT4();
            return posts.stream()
                    .map(post -> toListedPost(post, statsByCounterName, tags, categories, usersByName))
                    .toList();
        });
    }

    private Mono<Map<String, Stats>> fetchStatsByPostName(List<Post> posts) {
        var counterNames = posts.stream()
                .map(post -> MeterUtils.nameOf(Post.class, post.getMetadata().getName()))
                .toList();
        return counterService
                .getByNames(counterNames)
                .collectMap(counter -> counter.getMetadata().getName(), PostServiceImpl::toStats);
    }

    private Mono<Map<String, Contributor>> fetchUsersByName(List<String> usernames) {
        return userService.getUsersOrGhosts(usernames).collectList().map(users -> {
            var usersByName = new HashMap<String, Contributor>();
            users.forEach(user -> usersByName.putIfAbsent(user.getMetadata().getName(), toContributor(user)));
            return usersByName;
        });
    }

    private static ListedPost toListedPost(
            Post post,
            Map<String, Stats> statsByCounterName,
            List<Tag> tags,
            List<Category> categories,
            Map<String, Contributor> usersByName) {
        var listedPost = new ListedPost().setPost(post);

        var counterName = MeterUtils.nameOf(Post.class, post.getMetadata().getName());
        listedPost.setStats(statsByCounterName.getOrDefault(counterName, Stats.empty()));

        var postTagNames = post.getSpec().getTags();
        if (CollectionUtils.isEmpty(postTagNames)) {
            listedPost.setTags(List.of());
        } else {
            var postTagNameSet = new HashSet<>(postTagNames);
            listedPost.setTags(tags.stream()
                    .filter(tag -> postTagNameSet.contains(tag.getMetadata().getName()))
                    .toList());
        }

        var postCategoryNames = post.getSpec().getCategories();
        if (CollectionUtils.isEmpty(postCategoryNames)) {
            listedPost.setCategories(List.of());
        } else {
            var categoriesByName = new HashMap<String, Category>();
            categories.forEach(
                    category -> categoriesByName.put(category.getMetadata().getName(), category));
            listedPost.setCategories(new LinkedHashSet<>(postCategoryNames)
                    .stream()
                            .map(categoriesByName::get)
                            .filter(Objects::nonNull)
                            .toList());
        }

        var ghost = usersByName.get(UserService.GHOST_USER_NAME);
        listedPost.setOwner(resolveUser(usersByName, ghost, post.getSpec().getOwner()));

        var contributorNames = post.getStatusOrDefault().getContributors();
        if (CollectionUtils.isEmpty(contributorNames)) {
            listedPost.setContributors(List.of());
        } else {
            listedPost.setContributors(contributorNames.stream()
                    .map(name -> resolveUser(usersByName, ghost, name))
                    .filter(Objects::nonNull)
                    .toList());
        }
        return listedPost;
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

    private Flux<Tag> listTags(List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return Flux.empty();
        }
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(in("metadata.name", tagNames)));
        return client.listAll(Tag.class, listOptions, Sort.by("metadata.creationTimestamp"));
    }

    @Override
    public Flux<Category> listCategories(List<String> categoryNames) {
        if (CollectionUtils.isEmpty(categoryNames)) {
            return Flux.empty();
        }
        ToIntFunction<Category> comparator =
                category -> categoryNames.indexOf(category.getMetadata().getName());
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(in("metadata.name", categoryNames)));
        return client.listAll(Category.class, listOptions, Sort.unsorted()).sort(Comparator.comparingInt(comparator));
    }

    @Override
    public Mono<Post> draftPost(PostRequest postRequest) {
        return Mono.defer(() -> {
                    var post = postRequest.post();

                    if (StringUtils.isNotBlank(post.getSpec().getOwner())) {
                        return Mono.just(post);
                    }

                    return getContextUsername()
                            .doOnNext(username -> post.getSpec().setOwner(username))
                            .thenReturn(post);
                })
                .flatMap(client::create)
                .flatMap(post -> {
                    if (postRequest.content() == null) {
                        return Mono.just(post);
                    }
                    var contentRequest = new ContentRequest(
                            Ref.of(post),
                            post.getSpec().getHeadSnapshot(),
                            null,
                            postRequest.content().raw(),
                            postRequest.content().content(),
                            postRequest.content().rawType());
                    return draftContent(post.getSpec().getBaseSnapshot(), contentRequest)
                            .flatMap(contentWrapper -> waitForPostToDraftConcludingWork(
                                    post.getMetadata().getName(), contentWrapper));
                })
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<Post> waitForPostToDraftConcludingWork(String postName, ContentWrapper contentWrapper) {
        return Mono.defer(() -> client.fetch(Post.class, postName).flatMap(post -> {
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
        return updateContent(baseSnapshot, postRequest.contentRequest()).flatMap(contentWrapper -> {
            post.getSpec().setHeadSnapshot(contentWrapper.getSnapshotName());
            return client.update(post);
        });
    }

    @Override
    public Mono<Post> updateBy(Post post) {
        return client.update(post);
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(String postName) {
        return client.get(Post.class, postName).flatMap(this::getHeadContent);
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(Post post) {
        var headSnapshot = post.getSpec().getHeadSnapshot();
        return getContent(headSnapshot, post.getSpec().getBaseSnapshot());
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(String postName) {
        return client.get(Post.class, postName).flatMap(this::getReleaseContent);
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(Post post) {
        var releaseSnapshot = post.getSpec().getReleaseSnapshot();
        return getContent(releaseSnapshot, post.getSpec().getBaseSnapshot());
    }

    @Override
    public Flux<ListedSnapshotDto> listSnapshots(String name) {
        return client.fetch(Post.class, name)
                .flatMapMany(page -> listSnapshotsBy(Ref.of(page)))
                .map(ListedSnapshotDto::from);
    }

    @Override
    public Mono<Post> publish(Post post) {
        var spec = post.getSpec();
        spec.setPublish(true);
        if (spec.getHeadSnapshot() == null) {
            spec.setHeadSnapshot(spec.getBaseSnapshot());
        }
        spec.setReleaseSnapshot(spec.getHeadSnapshot());
        return client.update(post);
    }

    @Override
    public Mono<Post> unpublish(Post post) {
        post.getSpec().setPublish(false);
        return client.update(post);
    }

    @Override
    public Mono<Post> getByUsername(String postName, String username) {
        return client.get(Post.class, postName)
                .filter(post -> post.getSpec() != null)
                .filter(post -> Objects.equals(username, post.getSpec().getOwner()));
    }

    @Override
    public Mono<Post> revertToSpecifiedSnapshot(String postName, String snapshotName) {
        return client.get(Post.class, postName)
                .filter(post -> {
                    var head = post.getSpec().getHeadSnapshot();
                    return !StringUtils.equals(head, snapshotName);
                })
                .flatMap(post -> {
                    var baseSnapshot = post.getSpec().getBaseSnapshot();
                    return getContent(snapshotName, baseSnapshot)
                            .map(content -> ContentRequest.builder()
                                    .subjectRef(Ref.of(post))
                                    .headSnapshotName(post.getSpec().getHeadSnapshot())
                                    .content(content.getContent())
                                    .raw(content.getRaw())
                                    .rawType(content.getRawType())
                                    .build())
                            .flatMap(contentRequest -> draftContent(baseSnapshot, contentRequest))
                            .flatMap(content -> {
                                post.getSpec().setHeadSnapshot(content.getSnapshotName());
                                return publishPostWithRetry(post);
                            });
                });
    }

    @Override
    public Mono<ContentWrapper> deleteContent(String postName, String snapshotName) {
        return client.get(Post.class, postName)
                .flatMap(post -> {
                    var headSnapshotName = post.getSpec().getHeadSnapshot();
                    if (StringUtils.equals(headSnapshotName, snapshotName)) {
                        return updatePostWithRetry(post, record -> {
                            // update head to release
                            record.getSpec().setHeadSnapshot(record.getSpec().getReleaseSnapshot());
                            return record;
                        });
                    }
                    return Mono.just(post);
                })
                .flatMap(post -> {
                    var baseSnapshotName = post.getSpec().getBaseSnapshot();
                    var releaseSnapshotName = post.getSpec().getReleaseSnapshot();
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

    @Override
    public Mono<Post> recycleBy(String postName, String username) {
        return getByUsername(postName, username)
                .flatMap(post -> updatePostWithRetry(post, record -> {
                    record.getSpec().setDeleted(true);
                    return record;
                }));
    }

    private Mono<Post> updatePostWithRetry(Post post, UnaryOperator<Post> func) {
        return client.update(func.apply(post))
                .onErrorResume(
                        OptimisticLockingFailureException.class,
                        e -> Mono.defer(() -> client.get(
                                                Post.class, post.getMetadata().getName())
                                        .map(func)
                                        .flatMap(client::update))
                                .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                                        .filter(OptimisticLockingFailureException.class::isInstance)));
    }

    Mono<Post> publishPostWithRetry(Post post) {
        return publish(post)
                .onErrorResume(
                        OptimisticLockingFailureException.class,
                        e -> Mono.defer(() -> client.get(
                                                Post.class, post.getMetadata().getName())
                                        .flatMap(this::publish))
                                .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                                        .filter(OptimisticLockingFailureException.class::isInstance)));
    }
}
