package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.StatsVo;

@Component
@RequiredArgsConstructor
public class PostPublicQueryServiceImpl implements PostPublicQueryService {

    private final ReactiveExtensionClient client;

    private final TagFinder tagFinder;

    private final CategoryFinder categoryFinder;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    @Override
    public Mono<ListResult<ListedPostVo>> list(Integer page, Integer size,
        Predicate<Post> postPredicate, Comparator<Post> comparator) {
        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);
        return client.list(Post.class, predicate,
                comparator, pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> convertToListedPostVo(post)
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
    public Mono<ListedPostVo> convertToListedPostVo(@NonNull Post post) {
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
