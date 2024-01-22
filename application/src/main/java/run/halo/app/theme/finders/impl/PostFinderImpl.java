package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.NavigationPostVo;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.finders.vo.PostArchiveYearMonthVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.ReactiveQueryPostPredicateResolver;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("postFinder")
@AllArgsConstructor
public class PostFinderImpl implements PostFinder {

    private final ReactiveExtensionClient client;

    private final PostPublicQueryService postPublicQueryService;

    private final ReactiveQueryPostPredicateResolver postPredicateResolver;

    @Override
    public Mono<PostVo> getByName(String postName) {
        return postPredicateResolver.getPredicate()
            .flatMap(predicate -> client.get(Post.class, postName)
                .filter(predicate)
                .flatMap(post -> postPublicQueryService.convertToVo(post,
                    post.getSpec().getReleaseSnapshot())
                )
            );
    }

    @Override
    public Mono<ContentVo> content(String postName) {
        return postPublicQueryService.getContent(postName);
    }

    static Sort defaultSort() {
        return Sort.by(Sort.Order.desc("spec.pinned"),
            Sort.Order.desc("spec.priority"),
            Sort.Order.desc("spec.publishTime"),
            Sort.Order.desc("metadata.name")
        );
    }

    @NonNull
    static LinkNavigation findPostNavigation(List<String> postNames, String target) {
        Assert.notNull(target, "Target post name must not be null");
        for (int i = 0; i < postNames.size(); i++) {
            var item = postNames.get(i);
            if (target.equals(item)) {
                var prevLink = (i > 0) ? postNames.get(i - 1) : null;
                var nextLink = (i < postNames.size() - 1) ? postNames.get(i + 1) : null;
                return new LinkNavigation(prevLink, target, nextLink);
            }
        }
        return new LinkNavigation(null, target, null);
    }

    static Sort archiveSort() {
        return Sort.by(Sort.Order.desc("spec.publishTime"),
            Sort.Order.desc("metadata.name")
        );
    }

    private Mono<PostVo> fetchByName(String name) {
        if (StringUtils.isBlank(name)) {
            return Mono.empty();
        }
        return getByName(name)
            .onErrorResume(ExtensionNotFoundException.class::isInstance, (error) -> Mono.empty());
    }

    @Override
    public Mono<NavigationPostVo> cursor(String currentName) {
        return postPredicateResolver.getListOptions()
            .flatMapMany(postListOption ->
                client.listAll(Post.class, postListOption, defaultSort())
            )
            .map(post -> post.getMetadata().getName())
            .collectList()
            .flatMap(postNames -> Mono.just(NavigationPostVo.builder())
                .flatMap(builder -> getByName(currentName)
                    .doOnNext(builder::current)
                    .thenReturn(builder)
                )
                .flatMap(builder -> {
                    var previousNextPair = findPostNavigation(postNames, currentName);
                    String previousPostName = previousNextPair.prev();
                    String nextPostName = previousNextPair.next();
                    return fetchByName(previousPostName)
                        .doOnNext(builder::previous)
                        .then(fetchByName(nextPostName))
                        .doOnNext(builder::next)
                        .thenReturn(builder);
                })
                .map(NavigationPostVo.NavigationPostVoBuilder::build))
            .defaultIfEmpty(NavigationPostVo.empty());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> list(Integer page, Integer size) {
        return postPublicQueryService.list(new ListOptions(), getPageRequest(page, size));
    }

    private PageRequestImpl getPageRequest(Integer page, Integer size) {
        return PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), defaultSort());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByCategory(Integer page, Integer size,
        String categoryName) {
        var fieldQuery = QueryFactory.all();
        if (StringUtils.isNotBlank(categoryName)) {
            fieldQuery =
                QueryFactory.and(fieldQuery, QueryFactory.equal("spec.categories", categoryName));
        }
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return postPublicQueryService.list(listOptions, getPageRequest(page, size));
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByTag(Integer page, Integer size, String tag) {
        var fieldQuery = QueryFactory.all();
        if (StringUtils.isNotBlank(tag)) {
            fieldQuery =
                QueryFactory.and(fieldQuery, QueryFactory.equal("spec.tags", tag));
        }
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return postPublicQueryService.list(listOptions, getPageRequest(page, size));
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByOwner(Integer page, Integer size, String owner) {
        var fieldQuery = QueryFactory.all();
        if (StringUtils.isNotBlank(owner)) {
            fieldQuery =
                QueryFactory.and(fieldQuery, QueryFactory.equal("spec.owner", owner));
        }
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return postPublicQueryService.list(listOptions, getPageRequest(page, size));
    }

    @Override
    public Mono<ListResult<PostArchiveVo>> archives(Integer page, Integer size) {
        return archives(page, size, null, null);
    }

    @Override
    public Mono<ListResult<PostArchiveVo>> archives(Integer page, Integer size, String year) {
        return archives(page, size, year, null);
    }

    @Override
    public Mono<ListResult<PostArchiveVo>> archives(Integer page, Integer size, String year,
        String month) {
        var listOptions = new ListOptions();
        var labelSelectorBuilder = LabelSelector.builder();
        if (StringUtils.isNotBlank(year)) {
            labelSelectorBuilder.eq(Post.ARCHIVE_YEAR_LABEL, year);
        }
        if (StringUtils.isNotBlank(month)) {
            labelSelectorBuilder.eq(Post.ARCHIVE_MONTH_LABEL, month);
        }
        listOptions.setLabelSelector(labelSelectorBuilder.build());
        var pageRequest = PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), archiveSort());
        return postPublicQueryService.list(listOptions, pageRequest)
            .map(list -> {
                Map<String, List<ListedPostVo>> yearPosts = list.get()
                    .collect(Collectors.groupingBy(
                        post -> HaloUtils.getYearText(post.getSpec().getPublishTime())));
                List<PostArchiveVo> postArchives = yearPosts.entrySet().stream()
                    .map(entry -> {
                        String key = entry.getKey();
                        // archives by month
                        Map<String, List<ListedPostVo>> monthPosts = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                post -> HaloUtils.getMonthText(post.getSpec().getPublishTime())));
                        // convert to archive year month value objects
                        List<PostArchiveYearMonthVo> monthArchives = monthPosts.entrySet()
                            .stream()
                            .map(monthEntry -> PostArchiveYearMonthVo.builder()
                                .posts(monthEntry.getValue())
                                .month(monthEntry.getKey())
                                .build()
                            )
                            .sorted(
                                Comparator.comparing(PostArchiveYearMonthVo::getMonth).reversed())
                            .toList();
                        return PostArchiveVo.builder()
                            .year(String.valueOf(key))
                            .months(monthArchives)
                            .build();
                    })
                    .sorted(Comparator.comparing(PostArchiveVo::getYear).reversed())
                    .toList();
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postArchives);
            })
            .defaultIfEmpty(ListResult.emptyResult());
    }

    @Override
    public Flux<ListedPostVo> listAll() {
        return postPredicateResolver.getListOptions()
            .flatMapMany(listOptions -> client.listAll(Post.class, listOptions, defaultSort()))
            .concatMap(postPublicQueryService::convertToListedVo);
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }

    record LinkNavigation(String prev, String current, String next) {
    }
}
