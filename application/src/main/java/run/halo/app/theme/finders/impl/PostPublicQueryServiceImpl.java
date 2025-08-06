package run.halo.app.theme.finders.impl;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static run.halo.app.core.counter.MeterUtils.nameOf;
import static run.halo.app.core.user.service.UserService.GHOST_USER_NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ReactivePostContentHandler;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ContributorVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.StatsVo;
import run.halo.app.theme.router.ReactiveQueryPostPredicateResolver;

@Component
@RequiredArgsConstructor
public class PostPublicQueryServiceImpl implements PostPublicQueryService {

    private final ReactiveExtensionClient client;

    private final TagFinder tagFinder;

    private final CategoryFinder categoryFinder;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    private final PostService postService;

    private final ExtensionGetter extensionGetter;

    private final ReactiveQueryPostPredicateResolver postPredicateResolver;

    @Override
    public Mono<ListResult<ListedPostVo>> list(ListOptions queryOptions, PageRequest page) {
        return postPredicateResolver.getListOptions()
            .map(option -> {
                var fieldSelector = queryOptions.getFieldSelector();
                if (fieldSelector != null) {
                    option.setFieldSelector(option.getFieldSelector()
                        .andQuery(fieldSelector.query()));
                }
                var labelSelector = queryOptions.getLabelSelector();
                if (labelSelector != null) {
                    option.setLabelSelector(option.getLabelSelector().and(labelSelector));
                }
                return option;
            })
            .flatMap(listOptions -> client.listBy(Post.class, listOptions, page))
            .flatMap(list -> convertToListedVos(list.getItems())
                .map(
                    postVos -> new ListResult<>(
                        list.getPage(), list.getSize(), list.getTotal(), postVos
                    )
                )
            )
            .defaultIfEmpty(ListResult.emptyResult());
    }


    @Override
    public Mono<ListedPostVo> convertToListedVo(@NonNull Post post) {
        Assert.notNull(post, "Post must not be null");
        ListedPostVo postVo = ListedPostVo.from(post);
        postVo.setCategories(List.of());
        postVo.setTags(List.of());
        postVo.setContributors(List.of());

        return Mono.just(postVo)
            .flatMap(lp -> populateStats(postVo)
                .doOnNext(lp::setStats)
                .thenReturn(lp)
            )
            .flatMap(p -> {
                String owner = p.getSpec().getOwner();
                return contributorFinder.getContributor(owner)
                    .doOnNext(p::setOwner)
                    .thenReturn(p);
            })
            .flatMap(p -> {
                List<String> tagNames = p.getSpec().getTags();
                if (CollectionUtils.isEmpty(tagNames)) {
                    return Mono.just(p);
                }
                return tagFinder.getByNames(tagNames)
                    .collectList()
                    .doOnNext(p::setTags)
                    .thenReturn(p);
            })
            .flatMap(p -> {
                List<String> categoryNames = p.getSpec().getCategories();
                if (CollectionUtils.isEmpty(categoryNames)) {
                    return Mono.just(p);
                }
                return categoryFinder.getByNames(categoryNames)
                    .collectList()
                    .doOnNext(p::setCategories)
                    .thenReturn(p);
            })
            .flatMap(p -> contributorFinder.getContributors(p.getStatus().getContributors())
                .collectList()
                .doOnNext(p::setContributors)
                .thenReturn(p)
            )
            .defaultIfEmpty(postVo);
    }

    @Override
    public Mono<List<ListedPostVo>> convertToListedVos(List<Post> posts) {
        var counterNames = new HashSet<String>(posts.size());
        var userNames = new HashSet<String>();
        var tagNames = new HashSet<String>();
        var categoryNames = new HashSet<String>();
        posts.forEach(post -> {
            counterNames.add(nameOf(Post.class, post.getMetadata().getName()));
            var spec = post.getSpec();
            userNames.add(spec.getOwner());
            var status = post.getStatus();
            if (status != null && status.getContributors() != null) {
                userNames.addAll(status.getContributors());
            }
            if (spec.getTags() != null) {
                tagNames.addAll(spec.getTags());
            }
            if (spec.getCategories() != null) {
                categoryNames.addAll(spec.getCategories());
            }
        });

        var getCounters = counterService.getByNames(counterNames)
            .collectMap(counter -> counter.getMetadata().getName());
        var getContributors = contributorFinder.getContributors(userNames)
            .collectMap(ContributorVo::getName);
        var getTags = tagFinder.getByNames(tagNames)
            .collectMap(tagVo -> tagVo.getMetadata().getName());
        var getCategories = categoryFinder.getByNames(categoryNames)
            .collectMap(categoryVo -> categoryVo.getMetadata().getName());

        return Mono.zip(getCounters, getContributors, getTags, getCategories)
            .map(tuple -> {
                var counters = tuple.getT1();
                var contributors = tuple.getT2();
                var tags = tuple.getT3();
                var categories = tuple.getT4();
                return posts.stream()
                    .map(post -> {
                        var vo = ListedPostVo.from(post);
                        vo.setCategories(List.of());
                        vo.setTags(List.of());
                        vo.setContributors(List.of());

                        var spec = post.getSpec();
                        var status = post.getStatus();
                        var ghost = requireNonNullElseGet(
                            contributors.get(GHOST_USER_NAME), ContributorVo::ghost
                        );
                        vo.setOwner(requireNonNullElse(contributors.get(spec.getOwner()), ghost));
                        if (status != null && !CollectionUtils.isEmpty(status.getContributors())) {
                            vo.setContributors(status.getContributors()
                                .stream()
                                .map(name -> requireNonNullElse(contributors.get(name), ghost))
                                .toList());
                        }

                        if (!CollectionUtils.isEmpty(spec.getTags())) {
                            vo.setTags(spec.getTags()
                                .stream()
                                .map(tags::get)
                                .filter(Objects::nonNull)
                                .toList());
                        }
                        if (!CollectionUtils.isEmpty(spec.getCategories())) {
                            vo.setCategories(spec.getCategories()
                                .stream()
                                .map(categories::get)
                                .filter(Objects::nonNull)
                                .toList());
                        }

                        var counterName = nameOf(Post.class, post.getMetadata().getName());
                        var counter = counters.get(counterName);
                        if (counter != null) {
                            vo.setStats(StatsVo.builder()
                                .visit(counter.getVisit())
                                .upvote(counter.getUpvote())
                                .comment(counter.getApprovedComment())
                                .build()
                            );
                        } else {
                            vo.setStats(StatsVo.empty());
                        }

                        return vo;
                    })
                    .toList();
            });
    }

    @Override
    public Mono<PostVo> convertToVo(Post post, String snapshotName) {
        final String baseSnapshotName = post.getSpec().getBaseSnapshot();
        return convertToListedVo(post)
            .map(PostVo::from)
            .flatMap(postVo -> postService.getContent(snapshotName, baseSnapshotName)
                .flatMap(wrapper -> extendPostContent(post, wrapper))
                .doOnNext(postVo::setContent)
                .thenReturn(postVo)
            );
    }

    @Override
    public Mono<ContentVo> getContent(String postName) {
        return postPredicateResolver.getPredicate()
            .flatMap(predicate -> client.get(Post.class, postName)
                .filter(predicate)
            )
            .flatMap(post -> {
                String releaseSnapshot = post.getSpec().getReleaseSnapshot();
                return postService.getContent(releaseSnapshot, post.getSpec().getBaseSnapshot())
                    .flatMap(wrapper -> extendPostContent(post, wrapper));
            });
    }

    @NonNull
    protected Mono<ContentVo> extendPostContent(Post post,
        ContentWrapper wrapper) {
        Assert.notNull(post, "Post name must not be null");
        Assert.notNull(wrapper, "Post content must not be null");
        return extensionGetter.getEnabledExtensions(ReactivePostContentHandler.class)
            .reduce(Mono.fromSupplier(() -> ReactivePostContentHandler.PostContentContext.builder()
                    .post(post)
                    .content(wrapper.getContent())
                    .raw(wrapper.getRaw())
                    .rawType(wrapper.getRawType())
                    .build()
                ),
                (contentMono, handler) -> contentMono.flatMap(handler::handle)
            )
            .flatMap(Function.identity())
            .map(postContent -> ContentVo.builder()
                .content(postContent.getContent())
                .raw(postContent.getRaw())
                .build()
            );
    }

    private <T extends ListedPostVo> Mono<StatsVo> populateStats(T postVo) {
        return counterService.getByName(nameOf(Post.class, postVo.getMetadata()
                .getName())
            )
            .map(counter -> StatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(StatsVo.empty());
    }
}
