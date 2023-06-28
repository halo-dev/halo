package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ReactivePostContentHandler;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.StatsVo;

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

    @Override
    public Mono<ListResult<ListedPostVo>> list(Integer page, Integer size,
        Predicate<Post> postPredicate, Comparator<Post> comparator) {
        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);
        return client.list(Post.class, predicate,
                comparator, pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> convertToListedVo(post)
                    .flatMap(postVo -> populateStats(postVo)
                        .doOnNext(postVo::setStats).thenReturn(postVo)
                    )
                )
                .collectList()
                .map(postVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postVos)
                )
            )
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
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
    public Mono<PostVo> convertToVo(Post post, String snapshotName) {
        final String postName = post.getMetadata().getName();
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
        return client.get(Post.class, postName)
            .filter(FIXED_PREDICATE)
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
        return extensionGetter.getEnabledExtensionByDefinition(ReactivePostContentHandler.class)
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
        return counterService.getByName(MeterUtils.nameOf(Post.class, postVo.getMetadata()
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

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
