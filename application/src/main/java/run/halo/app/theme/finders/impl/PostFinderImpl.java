package run.halo.app.theme.finders.impl;

import static run.halo.app.extension.PageRequestImpl.ofSize;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.in;
import static run.halo.app.extension.index.query.Queries.notEqual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.CategoryService;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.SortUtils;
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

    private final CategoryService categoryService;

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
            Sort.Order.asc("metadata.name")
        );
    }

    static Sort archiveSort() {
        return Sort.by(Sort.Order.desc("spec.publishTime"),
            Sort.Order.desc("metadata.name")
        );
    }

    @Override
    public Mono<NavigationPostVo> cursor(String currentName) {
        return client.fetch(Post.class, currentName)
            // make sure the current post is published and has publishing time
            .filter(p -> Post.isPublished(p.getMetadata()))
            .filter(p -> p.getSpec() != null && p.getSpec().getPublishTime() != null)
            .flatMap(currentPost -> {
                var findPreviousPost = findPreviousPost(currentPost).map(Optional::of)
                    .defaultIfEmpty(Optional.empty());
                var findNextPost = findNextPost(currentPost).map(Optional::of)
                    .defaultIfEmpty(Optional.empty());
                return Mono.zip(findPreviousPost, findNextPost, (previous, next) ->
                    NavigationPostVo.builder()
                        .previous(previous.map(ListedPostVo::from).orElse(null))
                        .next(next.map(ListedPostVo::from).orElse(null))
                        .build()
                );
            })
            .switchIfEmpty(Mono.fromSupplier(NavigationPostVo::empty));
    }

    private Mono<Post> findPreviousPost(Post currentPost) {
        var publishTime = currentPost.getSpec().getPublishTime();
        return postPredicateResolver.getListOptions()
            .map(listOptions -> ListOptions.builder(listOptions)
                .andQuery(notHiddenPostQuery())
                .andQuery(Queries.lessThan("spec.publishTime", publishTime))
                .build()
            )
            .flatMap(listOptions -> {
                var sort = Sort.by(
                    Sort.Order.desc("spec.publishTime"),
                    Sort.Order.desc("metadata.name")
                );
                return client.listBy(
                    Post.class, listOptions, ofSize(1).withSort(sort)
                );
            })
            .flatMap(listResult -> Mono.justOrEmpty(listResult.getItems().stream().findFirst()));
    }

    private Mono<Post> findNextPost(Post currentPost) {
        var publishTime = currentPost.getSpec().getPublishTime();
        return postPredicateResolver.getListOptions()
            .map(listOptions -> ListOptions.builder(listOptions)
                .andQuery(notHiddenPostQuery())
                .andQuery(Queries.greaterThan("spec.publishTime", publishTime))
                .build()
            )
            .flatMap(listOptions -> {
                var sort = Sort.by(
                    Sort.Order.asc("spec.publishTime"),
                    Sort.Order.asc("metadata.name")
                );
                return client.listBy(
                    Post.class, listOptions, ofSize(1).withSort(sort)
                );
            })
            .flatMap(listResult -> Mono.justOrEmpty(listResult.getItems().stream().findFirst()));
    }

    private static Condition notHiddenPostQuery() {
        return notEqual("status.hideFromList", BooleanUtils.TRUE);
    }

    @Override
    public Mono<ListResult<ListedPostVo>> list(Map<String, Object> params) {
        var query = Optional.ofNullable(params)
            .map(map -> JsonUtils.mapToObject(map, PostQuery.class))
            .orElseGet(PostQuery::new);
        if (StringUtils.isNotBlank(query.getCategoryName())) {
            String categoryName = query.getCategoryName();
            return listChildrenCategories(categoryName)
                .map(category -> category.getMetadata().getName())
                .collectList()
                .flatMap(categoryNames -> {
                    var listOptions = ListOptions.builder(query.toListOptions())
                        .andQuery(in("spec.categories", categoryNames))
                        .build();
                    return postPublicQueryService.list(listOptions, query.toPageRequest())
                        .doOnNext(list -> list.forEach(
                            postVo -> sortCategoriesByFilter(postVo, categoryName, categoryNames)));
                });
        }
        return postPublicQueryService.list(query.toListOptions(), query.toPageRequest());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> list(Integer page, Integer size) {
        var listOptions = ListOptions.builder()
            .fieldQuery(notHiddenPostQuery())
            .build();
        return postPublicQueryService.list(listOptions, getPageRequest(page, size));
    }

    private PageRequestImpl getPageRequest(Integer page, Integer size) {
        return PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size), defaultSort());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByCategory(Integer page, Integer size,
        String categoryName) {
        return listChildrenCategories(categoryName)
            .map(category -> category.getMetadata().getName())
            .collectList()
            .flatMap(categoryNames -> {
                var listOptions = new ListOptions();
                var fieldQuery = in("spec.categories", categoryNames);
                listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
                return postPublicQueryService.list(listOptions, getPageRequest(page, size))
                    .doOnNext(list -> list.forEach(
                        postVo -> sortCategoriesByFilter(postVo, categoryName, categoryNames)));
            });
    }

    private Flux<Category> listChildrenCategories(String categoryName) {
        if (StringUtils.isBlank(categoryName)) {
            return client.listAll(Category.class, new ListOptions(),
                Sort.by(Sort.Order.asc("metadata.creationTimestamp"),
                    Sort.Order.desc("metadata.name")));
        }
        return categoryService.listChildren(categoryName);
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByTag(Integer page, Integer size, String tag) {
        var fieldQuery = Queries.empty();
        if (StringUtils.isNotBlank(tag)) {
            fieldQuery = fieldQuery.and(equal("spec.tags", tag));
        }
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return postPublicQueryService.list(listOptions, getPageRequest(page, size));
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByOwner(Integer page, Integer size, String owner) {
        var fieldQuery = Queries.empty();
        if (StringUtils.isNotBlank(owner)) {
            fieldQuery = fieldQuery.and(equal("spec.owner", owner));
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
        listOptions.setFieldSelector(FieldSelector.of(notHiddenPostQuery()));
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

    private static void sortCategoriesByFilter(ListedPostVo postVo, String queriedCategoryName,
        List<String> matchedCategoryNames) {
        var categories = postVo.getCategories();
        if (categories.size() <= 1) {
            return;
        }
        var matchedSet = new HashSet<>(matchedCategoryNames);
        var reordered = new ArrayList<>(categories);
        reordered.sort((a, b) -> {
            String nameA = a.getMetadata().getName();
            String nameB = b.getMetadata().getName();
            // Exact queried category comes first
            boolean aExact = queriedCategoryName.equals(nameA);
            boolean bExact = queriedCategoryName.equals(nameB);
            if (aExact && !bExact) {
                return -1;
            }
            if (!aExact && bExact) {
                return 1;
            }
            // Then matched (child) categories
            boolean aMatch = matchedSet.contains(nameA);
            boolean bMatch = matchedSet.contains(nameB);
            if (aMatch && !bMatch) {
                return -1;
            }
            if (!aMatch && bMatch) {
                return 1;
            }
            return 0;
        });
        postVo.setCategories(reordered);
    }

    @Override
    public Flux<ListedPostVo> listAll() {
        return postPredicateResolver.getListOptions()
            .flatMapMany(listOptions -> client.listAll(Post.class, listOptions, defaultSort()))
            .collectList()
            .flatMap(postPublicQueryService::convertToListedVos)
            .flatMapMany(Flux::fromIterable);
    }

    static int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    static int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }

    @Data
    public static class PostQuery {
        private Integer page;
        private Integer size;
        private String categoryName;
        private String tagName;
        private String owner;
        private List<String> sort;

        public ListOptions toListOptions() {
            var builder = ListOptions.builder();
            var hasQuery = false;
            if (StringUtils.isNotBlank(owner)) {
                builder.andQuery(equal("spec.owner", owner));
                hasQuery = true;
            }
            if (StringUtils.isNotBlank(tagName)) {
                builder.andQuery(equal("spec.tags", tagName));
                hasQuery = true;
            }
            // Exclude hidden posts when no query
            if (!hasQuery) {
                builder.fieldQuery(notHiddenPostQuery());
            }
            return builder.build();
        }

        public PageRequest toPageRequest() {
            return PageRequestImpl.of(pageNullSafe(getPage()),
                sizeNullSafe(getSize()), SortUtils.resolve(sort).and(defaultSort()));
        }
    }
}
