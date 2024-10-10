package run.halo.app.theme.finders.impl;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.counter.MeterUtils;
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
            .flatMap(list -> Flux.fromStream(list.get())
                .flatMapSequential(this::convertToListedVo)
                .collectList()
                .map(postVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postVos)
                )
            )
            .defaultIfEmpty(ListResult.emptyResult());
    }

    @Override
    public Mono<ListedPostVo> convertToListedVo(@NonNull Post post) {
        Assert.notNull(post, "Post must not be null");
        ListedPostVo postVo = ListedPostVo.from(post);

        var statsMono = fetchStats(post.getMetadata().getName())
            .doOnNext(postVo::setStats)
            .subscribeOn(Schedulers.boundedElastic());

        var ownerMono = contributorFinder.getContributor(post.getSpec().getOwner())
            .doOnNext(postVo::setOwner)
            .subscribeOn(Schedulers.boundedElastic());

        var tagMono = tagFinder.getByNames(post.getSpec().getTags())
            .collectList()
            .doOnNext(postVo::setTags)
            .subscribeOn(Schedulers.boundedElastic());

        var categoryMono = categoryFinder.getByNames(post.getSpec().getCategories())
            .collectList()
            .doOnNext(postVo::setCategories)
            .subscribeOn(Schedulers.boundedElastic());

        var contributorMono = contributorFinder.getContributors(
                post.getStatusOrDefault().getContributors())
            .collectList()
            .doOnNext(postVo::setContributors)
            .subscribeOn(Schedulers.boundedElastic());

        return Mono.when(statsMono, ownerMono, tagMono, categoryMono, contributorMono)
            .thenReturn(postVo);
    }

    @Override
    public Mono<PostVo> convertToVo(Post post, String snapshotName) {
        final String baseSnapshotName = post.getSpec().getBaseSnapshot();
        var listedVoMono = convertToListedVo(post).map(PostVo::from)
            .subscribeOn(Schedulers.boundedElastic());

        var contentMono = postService.getContent(snapshotName, baseSnapshotName)
            .flatMap(wrapper -> extendPostContent(post, wrapper))
            .switchIfEmpty(Mono.fromSupplier(ContentVo::empty))
            .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(listedVoMono, contentMono)
            .map(tuple -> {
                PostVo postVo = tuple.getT1();
                postVo.setContent(tuple.getT2());
                return postVo;
            });
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

    private Mono<StatsVo> fetchStats(String postName) {
        return counterService.getByName(MeterUtils.nameOf(Post.class, postName))
            .map(counter -> StatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(StatsVo.empty());
    }
}
