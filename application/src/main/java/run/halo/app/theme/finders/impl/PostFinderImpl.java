package run.halo.app.theme.finders.impl;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.ContributorFinder;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.NavigationPostVo;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.finders.vo.PostArchiveYearMonthVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.StatsVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("postFinder")
@AllArgsConstructor
public class PostFinderImpl implements PostFinder {

    public static final Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
        && Objects.equals(false, post.getSpec().getDeleted())
        && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());
    private final ReactiveExtensionClient client;

    private final PostService postService;

    private final TagFinder tagFinder;

    private final CategoryFinder categoryFinder;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    @Override
    public Mono<PostVo> getByName(String postName) {
        return client.fetch(Post.class, postName)
            .flatMap(this::getListedPostVo)
            .map(PostVo::from)
            .flatMap(postVo -> content(postName)
                .doOnNext(postVo::setContent)
                .thenReturn(postVo)
            );
    }

    @Override
    public Mono<ContentVo> content(String postName) {
        return postService.getReleaseContent(postName)
            .map(wrapper -> ContentVo.builder().content(wrapper.getContent())
                .raw(wrapper.getRaw()).build());
    }

    @Override
    public Mono<NavigationPostVo> cursor(String currentName) {
        // TODO Optimize the post names query here
        return client.list(Post.class, FIXED_PREDICATE, defaultComparator())
            .map(post -> post.getMetadata().getName())
            .collectList()
            .flatMap(postNames -> Mono.just(NavigationPostVo.builder())
                .flatMap(builder -> getByName(currentName)
                    .doOnNext(builder::current)
                    .thenReturn(builder)
                )
                .flatMap(builder -> {
                    Pair<String, String> previousNextPair =
                        postPreviousNextPair(postNames, currentName);
                    String previousPostName = previousNextPair.getLeft();
                    String nextPostName = previousNextPair.getRight();
                    return getByName(previousPostName)
                        .doOnNext(builder::previous)
                        .then(getByName(nextPostName))
                        .doOnNext(builder::next)
                        .thenReturn(builder);
                })
                .map(NavigationPostVo.NavigationPostVoBuilder::build))
            .defaultIfEmpty(NavigationPostVo.empty());
    }

    @Override
    public Flux<ListedPostVo> listAll() {
        return client.list(Post.class, FIXED_PREDICATE, defaultComparator())
            .concatMap(this::getListedPostVo);
    }

    static Pair<String, String> postPreviousNextPair(List<String> postNames,
        String currentName) {
        FixedSizeSlidingWindow<String> window = new FixedSizeSlidingWindow<>(3);
        for (String postName : postNames) {
            window.add(postName);
            if (!window.isFull()) {
                continue;
            }
            int index = window.indexOf(currentName);
            if (index == -1) {
                continue;
            }
            // got expected window
            if (index < 2) {
                break;
            }
        }

        List<String> elements = window.elements();
        // current post index
        int index = elements.indexOf(currentName);

        String previousPostName = null;
        if (index != 0) {
            previousPostName = elements.get(index - 1);
        }

        String nextPostName = null;
        if (elements.size() - 1 > index) {
            nextPostName = elements.get(index + 1);
        }
        return Pair.of(previousPostName, nextPostName);
    }

    static class FixedSizeSlidingWindow<T> {
        Deque<T> queue;
        int size;

        public FixedSizeSlidingWindow(int size) {
            this.size = size;
            // FIFO
            queue = new ArrayDeque<>(size);
        }

        /**
         * Add element to the window.
         * The element added first will be deleted when the element in the collection exceeds
         * {@code size}.
         */
        public void add(T t) {
            if (queue.size() == size) {
                // remove first
                queue.poll();
            }
            // add to last
            queue.add(t);
        }

        public int indexOf(T o) {
            List<T> elements = elements();
            return elements.indexOf(o);
        }

        public List<T> elements() {
            return new ArrayList<>(queue);
        }

        public boolean isFull() {
            return queue.size() == size;
        }
    }

    @Override
    public Mono<ListResult<ListedPostVo>> list(Integer page, Integer size) {
        return listPost(page, size, null, defaultComparator());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByCategory(Integer page, Integer size,
        String categoryName) {
        return listPost(page, size,
            post -> contains(post.getSpec().getCategories(), categoryName), defaultComparator());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByTag(Integer page, Integer size, String tag) {
        return listPost(page, size,
            post -> contains(post.getSpec().getTags(), tag), defaultComparator());
    }

    @Override
    public Mono<ListResult<ListedPostVo>> listByOwner(Integer page, Integer size, String owner) {
        return listPost(page, size,
            post -> post.getSpec().getOwner().equals(owner), defaultComparator());
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
        return listPost(page, size, post -> {
            Map<String, String> labels = post.getMetadata().getLabels();
            if (labels == null) {
                return false;
            }
            boolean yearMatch = StringUtils.isBlank(year)
                || year.equals(labels.get(Post.ARCHIVE_YEAR_LABEL));
            boolean monthMatch = StringUtils.isBlank(month)
                || month.equals(labels.get(Post.ARCHIVE_MONTH_LABEL));
            return yearMatch && monthMatch;
        }, archiveComparator())
            .map(list -> {
                Map<String, List<ListedPostVo>> yearPosts = list.get()
                    .collect(Collectors.groupingBy(
                        post -> HaloUtils.getYearText(post.getSpec().getPublishTime())));
                List<PostArchiveVo> postArchives =
                    yearPosts.entrySet().stream().map(entry -> {
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
            .defaultIfEmpty(new ListResult<>(page, size, 0, List.of()));
    }

    private boolean contains(List<String> c, String key) {
        if (StringUtils.isBlank(key) || c == null) {
            return false;
        }
        return c.contains(key);
    }

    private Mono<ListResult<ListedPostVo>> listPost(Integer page, Integer size,
        Predicate<Post> postPredicate,
        Comparator<Post> comparator) {
        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);
        return client.list(Post.class, predicate,
                comparator, pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> getListedPostVo(post)
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

    private <T extends ListedPostVo> Mono<StatsVo> populateStats(T postVo) {
        return counterService.getByName(MeterUtils.nameOf(Post.class, postVo.getMetadata()
                .getName()))
            .map(counter -> StatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(StatsVo.empty());
    }

    private Mono<ListedPostVo> getListedPostVo(@NonNull Post post) {
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

    static Comparator<Post> defaultComparator() {
        Function<Post, Boolean> pinned =
            post -> Objects.requireNonNullElse(post.getSpec().getPinned(), false);
        Function<Post, Integer> priority =
            post -> Objects.requireNonNullElse(post.getSpec().getPriority(), 0);
        Function<Post, Instant> publishTime =
            post -> post.getSpec().getPublishTime();
        Function<Post, String> name = post -> post.getMetadata().getName();
        return Comparator.comparing(pinned)
            .thenComparing(priority)
            .thenComparing(publishTime, Comparators.nullsLow())
            .thenComparing(name)
            .reversed();
    }

    static Comparator<Post> archiveComparator() {
        Function<Post, Instant> publishTime =
            post -> post.getSpec().getPublishTime();
        Function<Post, String> name = post -> post.getMetadata().getName();
        return Comparator.comparing(publishTime, Comparators.nullsLow())
            .thenComparing(name)
            .reversed();
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
