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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.comparator.Comparators;
import reactor.util.function.Tuple2;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Post;
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
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.Contributor;
import run.halo.app.theme.finders.vo.NavigationPostVo;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.finders.vo.PostArchiveYearMonthVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.StatsVo;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("postFinder")
public class PostFinderImpl implements PostFinder {

    public static final Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
            && Objects.equals(false, post.getSpec().getDeleted())
            && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());
    private final ReactiveExtensionClient client;

    private final ContentService contentService;

    private final TagFinder tagFinder;

    private final CategoryFinder categoryFinder;

    private final ContributorFinder contributorFinder;

    private final CounterService counterService;

    public PostFinderImpl(ReactiveExtensionClient client,
        ContentService contentService,
        TagFinder tagFinder,
        CategoryFinder categoryFinder,
        ContributorFinder contributorFinder, CounterService counterService) {
        this.client = client;
        this.contentService = contentService;
        this.tagFinder = tagFinder;
        this.categoryFinder = categoryFinder;
        this.contributorFinder = contributorFinder;
        this.counterService = counterService;
    }

    @Override
    public PostVo getByName(String postName) {
        Post post = client.fetch(Post.class, postName)
            .block();
        if (post == null) {
            return null;
        }
        PostVo postVo = getPostVo(post);
        postVo.setContent(content(postName));
        return postVo;
    }

    @Override
    public ContentVo content(String postName) {
        return client.fetch(Post.class, postName)
            .map(post -> post.getSpec().getReleaseSnapshot())
            .flatMap(contentService::getContent)
            .map(wrapper -> ContentVo.builder().content(wrapper.getContent())
                .raw(wrapper.getRaw()).build())
            .block();
    }

    @Override
    public NavigationPostVo cursor(String currentName) {
        // TODO Optimize the post names query here
        List<String> postNames = client.list(Post.class, FIXED_PREDICATE, defaultComparator())
            .map(post -> post.getMetadata().getName())
            .collectList()
            .block();
        if (postNames == null) {
            return NavigationPostVo.empty();
        }

        NavigationPostVo.NavigationPostVoBuilder builder = NavigationPostVo.builder()
            .current(getByName(currentName));

        Pair<String, String> previousNextPair = postPreviousNextPair(postNames, currentName);
        String previousPostName = previousNextPair.getLeft();
        String nextPostName = previousNextPair.getRight();

        if (previousPostName != null) {
            builder.previous(getByName(previousPostName));
        }

        if (nextPostName != null) {
            builder.next(getByName(nextPostName));
        }
        return builder.build();
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
        Tuple2<String, String> previousNext;
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
    public ListResult<PostVo> list(Integer page, Integer size) {
        return listPost(page, size, null, defaultComparator());
    }

    @Override
    public ListResult<PostVo> listByCategory(Integer page, Integer size, String categoryName) {
        return listPost(page, size,
            post -> contains(post.getSpec().getCategories(), categoryName), defaultComparator());
    }

    @Override
    public ListResult<PostVo> listByTag(Integer page, Integer size, String tag) {
        return listPost(page, size,
            post -> contains(post.getSpec().getTags(), tag), defaultComparator());
    }

    @Override
    public ListResult<PostArchiveVo> archives(Integer page, Integer size) {
        return archives(page, size, null, null);
    }

    @Override
    public ListResult<PostArchiveVo> archives(Integer page, Integer size, String year) {
        return archives(page, size, year, null);
    }

    @Override
    public ListResult<PostArchiveVo> archives(Integer page, Integer size, String year,
        String month) {
        ListResult<PostVo> list = listPost(page, size, post -> {
            Map<String, String> labels = post.getMetadata().getLabels();
            if (labels == null) {
                return false;
            }
            boolean yearMatch = StringUtils.isBlank(year)
                || year.equals(labels.get(Post.ARCHIVE_YEAR_LABEL));
            boolean monthMatch = StringUtils.isBlank(month)
                || month.equals(labels.get(Post.ARCHIVE_MONTH_LABEL));
            return yearMatch && monthMatch;
        }, archiveComparator());

        Map<String, List<PostVo>> yearPosts = list.get()
            .collect(Collectors.groupingBy(
                post -> HaloUtils.getYearText(post.getSpec().getPublishTime())));
        List<PostArchiveVo> postArchives =
            yearPosts.entrySet().stream().map(entry -> {
                String key = entry.getKey();
                // archives by month
                Map<String, List<PostVo>> monthPosts = entry.getValue().stream()
                    .collect(Collectors.groupingBy(
                        post -> HaloUtils.getMonthText(post.getSpec().getPublishTime())));
                // convert to archive year month value objects
                List<PostArchiveYearMonthVo> monthArchives = monthPosts.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(monthEntry -> PostArchiveYearMonthVo.builder()
                        .posts(monthEntry.getValue())
                        .month(monthEntry.getKey())
                        .build()
                    )
                    .toList();
                return PostArchiveVo.builder()
                    .year(String.valueOf(key))
                    .months(monthArchives)
                    .build();
            }).toList();
        return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), postArchives);
    }

    private boolean contains(List<String> c, String key) {
        if (StringUtils.isBlank(key) || c == null) {
            return false;
        }
        return c.contains(key);
    }

    private ListResult<PostVo> listPost(Integer page, Integer size, Predicate<Post> postPredicate,
        Comparator<Post> comparator) {
        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);
        ListResult<Post> list = client.list(Post.class, predicate,
                comparator, pageNullSafe(page), sizeNullSafe(size))
            .block();
        if (list == null) {
            return new ListResult<>(List.of());
        }
        List<PostVo> postVos = list.get()
            .map(this::getPostVo)
            .peek(this::populateStats)
            .toList();
        return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), postVos);
    }

    private void populateStats(PostVo postVo) {
        Counter counter =
            counterService.getByName(MeterUtils.nameOf(Post.class, postVo.getMetadata()
                .getName()));
        StatsVo statsVo = StatsVo.builder()
            .visit(counter.getVisit())
            .upvote(counter.getUpvote())
            .comment(counter.getApprovedComment())
            .build();
        postVo.setStats(statsVo);
    }

    private PostVo getPostVo(@NonNull Post post) {
        List<TagVo> tags = tagFinder.getByNames(post.getSpec().getTags());
        List<CategoryVo> categoryVos = categoryFinder.getByNames(post.getSpec().getCategories());
        List<Contributor> contributors =
            contributorFinder.getContributors(post.getStatus().getContributors());
        PostVo postVo = PostVo.from(post);
        postVo.setCategories(categoryVos);
        postVo.setTags(tags);
        postVo.setContributors(contributors);
        postVo.setOwner(contributorFinder.getContributor(post.getSpec().getOwner()));
        populateStats(postVo);
        return postVo;
    }

    static Comparator<Post> defaultComparator() {
        Function<Post, Boolean> pinned =
            post -> Objects.requireNonNullElse(post.getSpec().getPinned(), false);
        Function<Post, Integer> priority =
            post -> Objects.requireNonNullElse(post.getSpec().getPriority(), 0);
        Function<Post, Instant> creationTimestamp =
            post -> post.getMetadata().getCreationTimestamp();
        Function<Post, String> name = post -> post.getMetadata().getName();
        return Comparator.comparing(pinned)
            .thenComparing(priority)
            .thenComparing(creationTimestamp)
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
